# 逐步录制器（WORK-023）。由沙箱以 `python /tmp/trace.py` 运行，用户代码从 /tmp/user_main.py 读。
#
# 输出协议：**每行一个 JSON**，走标准错误（stdout 完全留给用户程序）。
# 事件形如：
#   {"t":"line","line":3,"locals":{"i":"0"},"out":"..."}   执行到某一行
#   {"t":"return","line":5,"locals":{...},"out":""}          函数返回
#   {"t":"exception","line":7,"locals":{...},"type":"ZeroDivisionError"}
#   {"t":"end","reason":"done|step_limit|time_limit|error","steps":12,"truncated":false}
#
# 三条硬上限（触顶即停止录制，**已录部分照样返回**）：
#   MAX_STEPS = 300      总步数
#   MAX_VARS  = 20       每步最多记多少个变量
#   MAX_VALUE = 60       每个变量值的最大字符数
#   MAX_BYTES = 256*1024 总输出字节
#
# 隔离纪律：
#   * 用户代码在独立命名空间里 exec，**看不到**本脚本的全局变量；
#   * 不把 trace 函数、文件句柄、录制器状态放进用户命名空间；
#   * builtins 给一份拷贝，用户改它不会影响录制器。
import json
import sys
import traceback

MAX_STEPS = 300
MAX_VARS = 20
MAX_VALUE = 60
MAX_BYTES = 256 * 1024

steps = 0
written = 0
truncated = False
stop_reason = "done"
_user_file = "/tmp/user_main.py"


def _render(value):
    """浅层 repr：容器只报类型与长度，不递归——循环引用会炸，巨型对象会拖死录制。"""
    try:
        if isinstance(value, (str, int, float, bool)) or value is None:
            text = repr(value)
        elif isinstance(value, (list, tuple, set, frozenset)):
            text = f"{type(value).__name__}(len={len(value)})"
        elif isinstance(value, dict):
            text = f"dict(len={len(value)})"
        else:
            text = f"<{type(value).__name__}>"
    except Exception:
        text = "<unrepresentable>"
    if len(text) > MAX_VALUE:
        return text[: MAX_VALUE - 1] + "…"
    return text


def _capture(frame):
    """抓当前帧的局部变量。跳过 dunder 名（__builtins__ 有 157 条，与教学无关）。"""
    names = [name for name in frame.f_locals.keys() if not name.startswith("__")]
    picked = names[:MAX_VARS]
    result = {}
    for name in picked:
        try:
            result[name] = _render(frame.f_locals[name])
        except Exception:
            result[name] = "<error>"
    if len(names) > MAX_VARS:
        result["…"] = f"另有 {len(names) - MAX_VARS} 个变量未显示"
    return result


def _emit(payload):
    global written, truncated
    if truncated:
        return
    try:
        line = json.dumps(payload, ensure_ascii=False)
    except Exception:
        return
    data = (line + "\n").encode("utf-8")
    if written + len(data) > MAX_BYTES:
        truncated = True
        return
    written += len(data)
    sys.stderr.write(line + "\n")
    sys.stderr.flush()


def _local_tracer(frame, event, arg):
    """settrace 的回调签名是 (frame, event, arg)。

    <p>只在**行事件**上记录，并且只看用户文件；记录器自己的帧一律放过
    （否则会记一堆无关调用，300 步天花板会被它们吃光）。
    """
    global steps, stop_reason
    if event != "line" or frame.f_code.co_filename != _user_file:
        return _local_tracer
    if steps >= MAX_STEPS:
        stop_reason = "step_limit"
        return None
    steps += 1
    _emit({"t": "line", "line": frame.f_lineno, "locals": _capture(frame), "out": ""})
    return _local_tracer


def main():
    global stop_reason
    try:
        source = open(_user_file, encoding="utf-8").read()
    except Exception as failure:
        _emit({"t": "end", "reason": "error", "steps": 0, "truncated": False,
               "message": f"读不到源码：{type(failure).__name__}"})
        return

    # 用户代码跑在独立命名空间：**不给它录制器的名字**
    user_globals = {"__name__": "__main__", "__file__": _user_file,
                    "__builtins__": __builtins__}
    sys.settrace(_local_tracer)
    try:
        compiled = compile(source, _user_file, "exec")
        exec(compiled, user_globals)
    except StopIteration:
        stop_reason = "done"
    except SystemExit:
        stop_reason = "done"
    except BaseException as failure:
        stop_reason = "error"
        tb = traceback.extract_tb(failure.__traceback__)
        line = tb[-1].lineno if tb else 0
        _emit({"t": "exception", "line": line, "locals": {}, "type": type(failure).__name__,
               "message": str(failure)[:120]})
    finally:
        sys.settrace(None)
    _emit({"t": "end", "reason": stop_reason, "steps": steps, "truncated": truncated})


main()
