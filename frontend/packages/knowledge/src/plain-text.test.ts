import { describe, expect, it } from 'vitest'

import { plainText } from './plain-text'

describe('plainText', () => {
  it('抹掉标题、强调与行内代码记号', () => {
    expect(plainText('## 为什么重要\n\n依赖的是 **接口** 而不是 `实现`')).toBe(
      '为什么重要 依赖的是 接口 而不是 实现',
    )
  })

  it('链接只留文字，图片整个丢掉', () => {
    expect(plainText('见 [官方文档](https://example.com/a) 与 ![图](/f/uuid.png)')).toBe(
      '见 官方文档 与',
    )
  })

  it('引用与列表记号不留痕，换行折成空格', () => {
    expect(plainText('> 注意\n\n- 第一条\n- 第二条')).toBe('注意 第一条 第二条')
  })

  it('空正文得到空串（卡片据此不渲染摘要那一行）', () => {
    expect(plainText('')).toBe('')
    expect(plainText('   \n\n  ')).toBe('')
  })

  it('**不**吞正文里的下划线与乘号（编程内容里它们是普通字符）', () => {
    expect(plainText('变量名 snake_case 是惯例')).toBe('变量名 snake_case 是惯例')
    expect(plainText('复杂度 O(n*m) 与 my_var_name')).toBe('复杂度 O(n*m) 与 my_var_name')
  })
})
