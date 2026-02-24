import request from '@/utils/request'
import type { ApiResponse, PageRequest, PageResponse } from '@/types/api'

// 面试状态
export type InterviewStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

// 题目类型
export type QuestionType = 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'TEXT'

// 面试记录
export interface Interview {
  id: number
  applicationId: number
  applicantName: string
  applicantAvatar?: string
  score?: number
  status: InterviewStatus
  startTime?: string
  endTime?: string
  aiReport?: string
  createdAt: string
}

// 面试题目
export interface InterviewQuestion {
  id: number
  interviewId: number
  questionId: number
  question: string
  questionType: QuestionType
  options?: string[]
  answer?: string
  score?: number
  maxScore: number
  aiComment?: string
}

// 题库题目
export interface Question {
  id: number
  category: string
  content: string
  questionType: QuestionType
  options?: string[]
  answer?: string
  keywords?: string[]
  score: number
  sort: number
  enabled: boolean
  createdAt: string
}

// 面试列表查询参数
export interface InterviewQueryParams extends PageRequest {
  status?: InterviewStatus
  startDate?: string
  endDate?: string
}

// 题库查询参数
export interface QuestionQueryParams extends PageRequest {
  category?: string
  questionType?: QuestionType
  enabled?: boolean
}

// 提交答案
export interface AnswerSubmit {
  questionId: number
  answer: string
}

// 获取面试列表
export function getInterviews(params: InterviewQueryParams) {
  return request.get<ApiResponse<PageResponse<Interview>>>('/interviews', { params })
}

// 获取面试详情
export function getInterviewById(id: number) {
  return request.get<ApiResponse<Interview>>(`/interviews/${id}`)
}

// 发起面试
export function startInterview(applicationId: number) {
  return request.post<ApiResponse<Interview>>('/interviews/start', { applicationId })
}

// 获取面试题目
export function getInterviewQuestions(interviewId: number) {
  return request.get<ApiResponse<InterviewQuestion[]>>(`/interviews/${interviewId}/questions`)
}

// 提交面试答案
export function submitInterview(interviewId: number, answers: AnswerSubmit[]) {
  return request.post<ApiResponse<Interview>>(`/interviews/${interviewId}/submit`, { answers })
}

// 获取题库列表
export function getQuestions(params: QuestionQueryParams) {
  return request.get<ApiResponse<PageResponse<Question>>>('/interview-questions', { params })
}

// 创建题目
export function createQuestion(data: Partial<Question>) {
  return request.post<ApiResponse<Question>>('/interview-questions', data)
}

// 更新题目
export function updateQuestion(id: number, data: Partial<Question>) {
  return request.put<ApiResponse<Question>>(`/interview-questions/${id}`, data)
}

// 删除题目
export function deleteQuestion(id: number) {
  return request.delete<ApiResponse<void>>(`/interview-questions/${id}`)
}

// 获取题目分类列表
export function getQuestionCategories() {
  return request.get<ApiResponse<string[]>>('/interview-questions/categories')
}
