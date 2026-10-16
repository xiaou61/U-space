/**
 * 测试环境补丁。
 *
 * <p>jsdom 不实现 `Element.prototype.scrollTo`（那是布局能力，jsdom 没有布局），而 streamdown
 * 在流式动效期间会调它做"只在用户本来就在底部时才跟着滚"。调用是 try 不住的异步 rAF，
 * 于是每个渲染周期都会抛一个未捕获异常，把整份报告淹掉——**而它掩盖的正是我们真正要看的断言**。
 *
 * <p>只补这一个方法，且补成空实现：我们断言的是结构，不是滚动位置。
 */
if (typeof Element.prototype.scrollTo !== 'function') {
  Element.prototype.scrollTo = () => undefined
}
