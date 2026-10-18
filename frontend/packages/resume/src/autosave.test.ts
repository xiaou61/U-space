import { describe, expect, it } from 'vitest'

import {
  acceptServerRevision,
  beginSave,
  forceOverwrite,
  hasUnsavedWork,
  initialState,
  markDirty,
  saveFailed,
  saveSucceeded,
} from './autosave'

/**
 * 自动保存状态机（WORK-018 AC-002）。
 *
 * <p>重点在最后两组：失败与冲突时**内容不被回填**——这是本项目已经踩过一次的坑。
 */
describe('简历自动保存状态机', () => {
  it('初始是 idle，没有保存时间', () => {
    const state = initialState(1)
    expect(state.status).toBe('idle')
    expect(state.savedAt).toBeNull()
    expect(state.revision).toBe(1)
  })

  it('改动 → dirty；开始保存 → saving；成功 → saved 并记住 revision', () => {
    let state = markDirty(initialState(1))
    expect(state.status).toBe('dirty')

    state = beginSave(state)
    expect(state.status).toBe('saving')

    state = saveSucceeded(state, 2, 1000)
    expect(state.status).toBe('saved')
    expect(state.revision).toBe(2)
    expect(state.savedAt).toBe(1000)
  })

  it('保存中又改了：状态回到 dirty，这样保存结束后会再存一次', () => {
    const state = markDirty(beginSave(markDirty(initialState(1))))
    expect(state.status).toBe('dirty')
  })

  it('保存失败：状态是 error，但 revision 不变、内容不动', () => {
    const state = saveFailed(beginSave(markDirty(initialState(3))), '网络断了', false)
    expect(state.status).toBe('error')
    expect(state.error).toBe('网络断了')
    expect(state.revision).toBe(3)
  })

  it('409 冲突：状态是 conflict，不是普通 error（界面要给出两个选项）', () => {
    const state = saveFailed(beginSave(markDirty(initialState(3))), '这份简历在别处被改过了', true)
    expect(state.status).toBe('conflict')
  })

  it('冲突后"重新加载"：接受服务端 revision，状态回到 idle', () => {
    const conflicted = saveFailed(initialState(3), '冲突', true)
    const state = acceptServerRevision(conflicted, 7)
    expect(state.status).toBe('idle')
    expect(state.revision).toBe(7)
    expect(state.error).toBeNull()
  })

  it('冲突后"用我的版本覆盖"：拿服务端 revision 直接重试，不丢内容', () => {
    const conflicted = saveFailed(initialState(3), '冲突', true)
    const state = forceOverwrite(conflicted, 7)
    expect(state.status).toBe('dirty')
    expect(state.revision).toBe(7)
  })

  it('有未保存内容时要拦离开：dirty / saving / error / conflict 都算', () => {
    expect(hasUnsavedWork({ status: 'dirty', savedAt: null, error: null, revision: 1 })).toBe(true)
    expect(hasUnsavedWork({ status: 'saving', savedAt: null, error: null, revision: 1 })).toBe(true)
    expect(hasUnsavedWork({ status: 'error', savedAt: null, error: 'x', revision: 1 })).toBe(true)
    expect(hasUnsavedWork({ status: 'conflict', savedAt: null, error: 'x', revision: 1 })).toBe(true)
    expect(hasUnsavedWork({ status: 'saved', savedAt: 1, error: null, revision: 2 })).toBe(false)
    expect(hasUnsavedWork(initialState(1))).toBe(false)
  })
})
