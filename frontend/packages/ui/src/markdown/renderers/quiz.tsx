import { useMemo, useState } from 'react'
import type { CustomRendererProps } from 'streamdown'

import { BlockFallback, BlockFrame, Pill } from './blocks'
import { useSourceReading } from './context'
import { parseQuiz, scoreQuiz } from './schema'

/**
 * 小测（WORK-014）：逐题即时反馈 + 块末得分。
 *
 * <p>成绩只写本地进度，不上传。对错**不只靠颜色**：文字里带 ✓ / ✗，读屏与色觉障碍都能判断。
 */
export function Quiz({ code }: CustomRendererProps) {
  const parsed = useMemo(() => parseQuiz(code), [code])
  const { progress, renderText, recordAttempt } = useSourceReading()
  const [answers, setAnswers] = useState<(number | null)[]>([])

  if (!parsed.ok) {
    return <BlockFallback title="小测" error={parsed.error} raw={code} />
  }
  const quiz = parsed.value
  const answered = answers.filter((answer) => answer !== null).length
  const result = scoreQuiz(quiz, answers)
  const complete = answered === quiz.questions.length

  const choose = (questionIndex: number, optionIndex: number) => {
    setAnswers((previous) => {
      const next = [...previous]
      if (next[questionIndex] !== null && next[questionIndex] !== undefined) {
        return previous
      }
      next[questionIndex] = optionIndex
      // 每次首次选择都上报一次；对错由服务端按正文判定，前端只发选择了哪个
      if (quiz.id !== undefined) {
        recordAttempt?.({
          blockKey: quiz.id,
          questionKey: `${quiz.id}#${questionIndex}`,
          choice: optionIndex,
        })
      }
      const finished = next.filter((answer) => answer !== null && answer !== undefined).length === quiz.questions.length
      if (finished && quiz.id !== undefined) {
        const final = scoreQuiz(quiz, next)
        progress?.update({ quizzes: { [quiz.id]: { score: final.correct, total: final.total } } })
      }
      return next
    })
  }

  const reset = () => setAnswers([])

  return (
    <BlockFrame
      title={quiz.title ?? '小测'}
      testId="quiz"
      actions={
        <Pill tone={complete ? 'success' : 'muted'}>
          {complete ? `${result.correct} / ${result.total} 答对` : `已答 ${answered} / ${quiz.questions.length}`}
        </Pill>
      }
    >
      <ol className="divide-border divide-y">
        {quiz.questions.map((question, questionIndex) => {
          const choice = answers[questionIndex]
          const locked = choice !== null && choice !== undefined
          return (
            <li key={questionIndex} className="p-4">
              <p className="mb-2 text-sm font-medium">
                {questionIndex + 1}. {question.question}
              </p>
              <div className="grid gap-1.5">
                {question.options.map((option, optionIndex) => {
                  const chosen = choice === optionIndex
                  const isAnswer = question.answer === optionIndex
                  return (
                    <button
                      key={optionIndex}
                      type="button"
                      data-testid={`quiz-option-${questionIndex}-${optionIndex}`}
                      disabled={locked}
                      onClick={() => choose(questionIndex, optionIndex)}
                      className={`min-h-11 rounded-md border px-3 py-2 text-left text-sm transition-colors ${
                        locked && isAnswer
                          ? 'border-success bg-success/10 text-success'
                          : locked && chosen
                            ? 'border-destructive bg-destructive/10 text-destructive'
                            : 'border-border bg-background hover:bg-muted'
                      }`}
                    >
                      {locked && isAnswer ? '✓ ' : ''}
                      {locked && chosen && !isAnswer ? '✗ ' : ''}
                      {option}
                    </button>
                  )
                })}
              </div>
              {locked ? (
                <div className="text-muted-foreground mt-2 text-xs leading-6" data-testid={`quiz-explain-${questionIndex}`}>
                  {choice === question.answer ? '答对了。' : '这题不对。'}
                  {question.explain === undefined ? null : (
                    <span className="ml-1">
                      {renderText === undefined ? question.explain : renderText(question.explain)}
                    </span>
                  )}
                </div>
              ) : null}
            </li>
          )
        })}
      </ol>
      {complete ? (
        <div className="border-border flex items-center gap-3 border-t px-4 py-2.5 text-sm">
          <span data-testid="quiz-score">
            答对 {result.correct} / {result.total}
          </span>
          <button type="button" onClick={reset} className="text-muted-foreground ml-auto inline-flex min-h-11 items-center text-xs underline underline-offset-2">
            重做
          </button>
        </div>
      ) : null}
    </BlockFrame>
  )
}
