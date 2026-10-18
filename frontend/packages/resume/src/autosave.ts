/**
 * 自动保存的状态机（WORK-018 REQ-006 / AC-002）。
 *
 * <p><b>为什么单独抽出来并单独测</b>：这个文件里那条"失败时不回填"的规则，是本项目已经
 * 踩过一次的坑（AGENTS.md 记过：同步 effect 把正在打的字冲掉）。用组件测它需要挂载、
 * 假定时器与网络桩三件事一起搭；抽成纯状态机之后，一条 `expect` 就能钉住。
 *
 * <p>三种状态对用户可见，界面按它显示"保存中 / 已保存 / 未保存"：
 * <ul>
 *   <li>{@code dirty}：有改动还没写进服务端。</li>
 *   <li>{@code saving}：请求在飞。</li>
 *   <li>{@code saved}：服务端已确认。</li>
 *   <li>{@code error}：上一次保存失败，**内容仍在内存里**，不覆盖。</li>
 *   <li>{@code conflict}：服务端说 revision 被别人推进了（409）。</li>
 * </ul>
 */

export type AutosaveStatus = 'idle' | 'dirty' | 'saving' | 'saved' | 'error' | 'conflict'

export interface AutosaveState {
  status: AutosaveStatus
  /** 成功保存的时间；从未成功过为 null。 */
  savedAt: number | null
  /** 最后一次失败的原因（给界面显示，不吞掉）。 */
  error: string | null
  /** 服务端已确认的 revision；保存时原样带回。 */
  revision: number
}

/**
 * 判据：**有改动就 dirty**。这里不做"内容相同就跳过"的比较——那需要深比较整个简历，
 * 而它换来的只是少一次 PUT；反而会让"改了又改回原样"这种场景的行为变得难以解释。
 */
export function markDirty(state: AutosaveState): AutosaveState {
  if (state.status === 'saving') {
    // 保存中又改了：仍然是 saving（让在飞的那次先落地），但状态记为 dirty，
    // 这样它一结束就会再存一次。
    return { ...state, status: 'dirty' }
  }
  return { ...state, status: 'dirty', error: null }
}

export function beginSave(state: AutosaveState): AutosaveState {
  return { ...state, status: 'saving', error: null }
}

export function saveSucceeded(state: AutosaveState, revision: number, now: number): AutosaveState {
  return { ...state, status: 'saved', revision, savedAt: now, error: null }
}

/**
 * 保存失败。
 *
 * <p><b>只改状态，不动内容</b>：调用方必须保留用户输入框里的值。这是本文件存在的理由——
 * 一个"失败后重新拉服务端数据回填"的做法会把用户正在打的字冲掉。
 */
export function saveFailed(state: AutosaveState, message: string, conflict: boolean): AutosaveState {
  return { ...state, status: conflict ? 'conflict' : 'error', error: message }
}

/** 冲突后用户选择"重新加载"：接受服务端的 revision，但**不**改用户当前输入（由界面提示他放弃修改）。 */
export function acceptServerRevision(state: AutosaveState, revision: number): AutosaveState {
  return { ...state, status: 'idle', revision, error: null }
}

/** 冲突后用户选择"用我的版本覆盖"：用服务端最新 revision 重新提交。 */
export function forceOverwrite(state: AutosaveState, serverRevision: number): AutosaveState {
  return { ...state, revision: serverRevision, status: 'dirty', error: null }
}

/** 保存中 / 未保存时离开页面要拦一下。 */
export function hasUnsavedWork(state: AutosaveState): boolean {
  return state.status === 'dirty' || state.status === 'saving' || state.status === 'error'
        || state.status === 'conflict'
}

export function initialState(revision: number): AutosaveState {
  return { status: 'idle', savedAt: null, error: null, revision }
}
