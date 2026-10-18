import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'

import { Dialog, DialogContent, DialogDescription, DialogTitle } from './dialog'

/**
 * 对话框形态（WORK-016 / AC-013）。守三件事：
 * 1. 窄屏形态由组件提供，调用点不必各写一套；
 * 2. 桌面形态与改动前一致（居中浮层 + 可被 `sm:max-w-*` 覆盖）；
 * 3. **不覆盖调用点自己的桌面限高与滚动**——这是本轮实测踩到的真缺陷。
 */
function renderDialog() {
  return render(
    <Dialog open>
      <DialogContent data-testid="dlg">
        <DialogTitle>标题</DialogTitle>
        <DialogDescription>说明</DialogDescription>
      </DialogContent>
    </Dialog>,
  )
}

// 这个仓库没有全局 auto-cleanup，同文件多个 render 会叠在同一个 body 上
afterEach(cleanup)

describe('DialogContent 形态', () => {
  it('带窄屏贴底形态（满宽、只保留上方圆角、按动态视口限高）', () => {
    renderDialog()
    const cls = screen.getByTestId('dlg').className
    for (const token of [
      'max-sm:inset-x-0',
      'max-sm:bottom-0',
      'max-sm:max-w-none',
      'max-sm:rounded-t-2xl',
      'max-sm:max-h-[92dvh]',
      'max-sm:overflow-y-auto',
    ]) {
      expect(cls, `缺 ${token}：手机上是飘在中间的小窗`).toContain(token)
    }
  })

  it('桌面保持居中浮层，默认宽度仍可被调用点覆盖', () => {
    renderDialog()
    const cls = screen.getByTestId('dlg').className
    expect(cls).toContain('top-1/2')
    expect(cls).toContain('left-1/2')
    expect(cls).toContain('-translate-x-1/2')
    expect(cls).toContain('rounded-xl')
    expect(cls).toContain('sm:max-w-sm')
  })

  it('不覆盖调用点的桌面限高与滚动', () => {
    /*
     * 回归守卫：条目编辑器传 `max-h-[88vh] overflow-y-auto`。如果这里用无前缀的
     * `max-h-*` / `overflow-*` 再在 sm 复位，会把调用点那两个类一起干掉——
     * 实测结果是对话框长过视口、底部保存按钮点不到（管理端两条 e2e 因此超时）。
     */
    renderDialog()
    const cls = screen.getByTestId('dlg').className
    expect(cls).not.toMatch(/(^|\s)max-h-\[/)
    expect(cls).not.toMatch(/(^|\s)overflow-y-auto/)
    expect(cls).not.toMatch(/sm:max-h-none/)
    expect(cls).not.toMatch(/sm:overflow-visible/)
  })

  it('窄屏用 dvh 而不是 vh，避免地址栏收放时被裁', () => {
    renderDialog()
    const cls = screen.getByTestId('dlg').className
    expect(cls).not.toMatch(/\[\d+vh\]/)
  })
})
