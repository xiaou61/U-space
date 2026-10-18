import * as React from "react"
import { Dialog as DialogPrimitive } from "@base-ui/react/dialog"
import { cn } from "cn"

import { Button } from "./button"
import { XIcon } from "lucide-react"

function Dialog({ ...props }: DialogPrimitive.Root.Props) {
  return <DialogPrimitive.Root data-slot="dialog" {...props} />
}

function DialogTrigger({ ...props }: DialogPrimitive.Trigger.Props) {
  return <DialogPrimitive.Trigger data-slot="dialog-trigger" {...props} />
}

function DialogPortal({ ...props }: DialogPrimitive.Portal.Props) {
  return <DialogPrimitive.Portal data-slot="dialog-portal" {...props} />
}

function DialogClose({ ...props }: DialogPrimitive.Close.Props) {
  return <DialogPrimitive.Close data-slot="dialog-close" {...props} />
}

function DialogOverlay({
  className,
  ...props
}: DialogPrimitive.Backdrop.Props) {
  return (
    <DialogPrimitive.Backdrop
      data-slot="dialog-overlay"
      className={cn(
        "fixed inset-0 isolate z-50 bg-overlay duration-100 supports-backdrop-filter:backdrop-blur-xs data-open:animate-in data-open:fade-in-0 data-closed:animate-out data-closed:fade-out-0",
        className
      )}
      {...props}
    />
  )
}

/**
 * 对话框内容。
 *
 * <p><b>两种形态（WORK-016）</b>：窄屏（&lt;sm）是**贴底、占满宽度、可滚动**的面板；宽屏是
 * **居中浮层**，与既有观感一致。
 *
 * <p>为什么形态由组件决定而不是每个调用点自己写断点类：仓库里有 6 处调用，各自补一套
 * `max-sm:` 就是 6 份重复，且新页面一定会漏。调用点的 `className` 只表达**桌面宽度**
 * （`sm:max-w-3xl` 之类），窄屏形态统一在这里（AC-013）。
 *
 * <p><b>窄屏全部用 `max-sm:` 前缀，桌面类一个不覆盖。</b>这不是风格选择：条目编辑器这类
 * 调用点自己写了 `max-h-[88vh] overflow-y-auto`，如果这里用无前缀的 `max-h` 再在 `sm` 复位，
 * 复位的优先级会把它**一起干掉**——实测后果是对话框长到视口之外、底部的保存按钮点不到，
 * 管理端两条 e2e 因此超时。前缀式只加窄屏行为，桌面完全继承原样。
 *
 * <p>窄屏用 `max-h-[92dvh]`：`dvh` 跟着地址栏收放变，`vh` 会让底部按钮被地址栏吃掉。
 */
function DialogContent({
  className,
  children,
  showCloseButton = true,
  ...props
}: DialogPrimitive.Popup.Props & {
  showCloseButton?: boolean
}) {
  return (
    <DialogPortal>
      <DialogOverlay />
      <DialogPrimitive.Popup
        data-slot="dialog-content"
        className={cn(
          // 桌面：居中浮层（与改动前逐字一致）
          "fixed top-1/2 left-1/2 z-50 grid w-full max-w-[calc(100%-2rem)] -translate-x-1/2 -translate-y-1/2 gap-4 rounded-xl bg-popover p-4 text-sm text-popover-foreground ring-1 ring-foreground/10 duration-100 outline-none sm:max-w-sm",
          // 窄屏：贴底、满宽、可滚、只保留上方圆角
          "max-sm:inset-x-0 max-sm:bottom-0 max-sm:top-auto max-sm:left-0 max-sm:max-h-[92dvh] max-sm:max-w-none max-sm:translate-x-0 max-sm:translate-y-0 max-sm:overflow-y-auto max-sm:rounded-t-2xl max-sm:rounded-b-none",
          "data-open:animate-in data-open:fade-in-0 data-closed:animate-out data-closed:fade-out-0",
          "sm:data-open:zoom-in-95 sm:data-closed:zoom-out-95",
          "max-sm:data-open:slide-in-from-bottom max-sm:data-closed:slide-out-to-bottom",
          className
        )}
        {...props}
      >
        {/* 抓手的唯一作用是让"这是个从底部升起的面板"看得出来；不可操作，读屏隐藏 */}
        <span
          aria-hidden
          data-slot="dialog-handle"
          className="bg-border mx-auto block h-1 w-10 shrink-0 rounded-full sm:hidden"
        />
        {children}
        {showCloseButton && (
          <DialogPrimitive.Close
            data-slot="dialog-close"
            render={
              <Button
                variant="ghost"
                // 窄屏 44px 命中区：视觉仍是 28px，用伪元素把可点区撑到 44
                className="absolute top-2 right-2 max-sm:after:absolute max-sm:after:-inset-2 max-sm:after:content-['']"
                size="icon-sm"
              />
            }
          >
            <XIcon
            />
            <span className="sr-only">Close</span>
          </DialogPrimitive.Close>
        )}
      </DialogPrimitive.Popup>
    </DialogPortal>
  )
}

function DialogHeader({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      data-slot="dialog-header"
      className={cn("flex flex-col gap-2", className)}
      {...props}
    />
  )
}

function DialogFooter({
  className,
  showCloseButton = false,
  children,
  ...props
}: React.ComponentProps<"div"> & {
  showCloseButton?: boolean
}) {
  /**
   * 窄屏下这个页脚**常驻可见**（样式在 globals.css 的 `[data-slot='dialog-footer']` 规则里）：
   * 管理端条目编辑器比手机屏幕高，保存/取消滚到最底下才出现等于"每次保存都要先滚一屏"。
   * 用 CSS 规则而不是工具类，是因为要同时改 `position`、`bottom` 与带 `env()` 的 padding-bottom，
   * 三个属性里最后一个与 `p-4` 同属性，谁生效取决于生成顺序——写成明确规则就不用赌。
   */
  return (
    <div
      data-slot="dialog-footer"
      className={cn(
        "-mx-4 -mb-4 flex flex-col-reverse gap-2 rounded-b-xl border-t bg-muted/50 p-4 sm:flex-row sm:justify-end",
        className
      )}
      {...props}
    >
      {children}
      {showCloseButton && (
        <DialogPrimitive.Close render={<Button variant="outline" />}>
          Close
        </DialogPrimitive.Close>
      )}
    </div>
  )
}

function DialogTitle({ className, ...props }: DialogPrimitive.Title.Props) {
  return (
    <DialogPrimitive.Title
      data-slot="dialog-title"
      className={cn(
        "text-base leading-none font-medium",
        className
      )}
      {...props}
    />
  )
}

function DialogDescription({
  className,
  ...props
}: DialogPrimitive.Description.Props) {
  return (
    <DialogPrimitive.Description
      data-slot="dialog-description"
      className={cn(
        "text-sm text-muted-foreground *:[a]:underline *:[a]:underline-offset-3 *:[a]:hover:text-foreground",
        className
      )}
      {...props}
    />
  )
}

export {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogOverlay,
  DialogPortal,
  DialogTitle,
  DialogTrigger,
}
