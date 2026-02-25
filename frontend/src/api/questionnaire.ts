import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/api'

// 字段类型
export type FieldType = 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'TEXT' | 'DATE' | 'NUMBER' | 'DROPDOWN' | 'GROUP'

// 问卷状态
export type QuestionnaireStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'

// 访问类型
export type AccessType = 'REGISTER' | 'PUBLIC_LINK' | 'INTERNAL'

// 问卷字段
export interface QuestionnaireField {
  id?: number
  fieldKey: string
  label: string
  fieldType: FieldType
  options?: any[]
  validationRules?: Record<string, any>
  required: boolean
  sortOrder: number
  conditionLogic?: Record<string, any>
  groupId?: number
}

// 问卷
export interface Questionnaire {
  id?: number
  title: string
  description?: string
  status: QuestionnaireStatus
  accessType: AccessType
  publicToken?: string
  version: number
  fields?: QuestionnaireField[]
  createdAt?: string
  updatedAt?: string
}

// 问卷表单
export interface QuestionnaireForm {
  title: string
  description?: string
  status: QuestionnaireStatus
  accessType: AccessType
  fields?: QuestionnaireField[]
}

// 问卷查询参数
export interface QuestionnaireQuery {
  page?: number
  size?: number
  status?: QuestionnaireStatus
  keyword?: string
}

// 问卷回复
export interface QuestionnaireResponse {
  id?: number
  questionnaireId: number
  answers: Record<string, any>
  respondentInfo?: Record<string, any>
  submittedAt?: string
}

// 获取问卷列表
export function getQuestionnaires(params?: QuestionnaireQuery) {
  return request.get<ApiResponse<PageResponse<Questionnaire>>>('/questionnaires', { params })
}

// 获取问卷详情
export function getQuestionnaire(id: number) {
  return request.get<ApiResponse<Questionnaire>>(`/questionnaires/${id}`)
}

// 创建问卷
export function createQuestionnaire(data: QuestionnaireForm) {
  return request.post<ApiResponse<Questionnaire>>('/questionnaires', data)
}

// 更新问卷
export function updateQuestionnaire(id: number, data: QuestionnaireForm) {
  return request.put<ApiResponse<Questionnaire>>(`/questionnaires/${id}`, data)
}

// 删除问卷
export function deleteQuestionnaire(id: number) {
  return request.delete<ApiResponse<void>>(`/questionnaires/${id}`)
}

// 发布问卷
export function publishQuestionnaire(id: number) {
  return request.post<ApiResponse<Questionnaire>>(`/questionnaires/${id}/publish`)
}

// 归档问卷
export function archiveQuestionnaire(id: number) {
  return request.post<ApiResponse<Questionnaire>>(`/questionnaires/${id}/archive`)
}

// 获取公开问卷（通过token）
export function getPublicQuestionnaire(token: string) {
  return request.get<ApiResponse<Questionnaire>>(`/questionnaires/public/${token}`)
}

// 提交问卷回复
export function submitQuestionnaireResponse(questionnaireId: number, data: Omit<QuestionnaireResponse, 'id' | 'questionnaireId' | 'submittedAt'>) {
  return request.post<ApiResponse<QuestionnaireResponse>>(`/questionnaires/${questionnaireId}/responses`, data)
}

// 获取问卷回复列表
export function getQuestionnaireResponses(questionnaireId: number, params?: { page?: number; size?: number }) {
  return request.get<ApiResponse<PageResponse<QuestionnaireResponse>>>(`/questionnaires/${questionnaireId}/responses`, { params })
}

// 获取单个回复详情
export function getQuestionnaireResponse(questionnaireId: number, responseId: number) {
  return request.get<ApiResponse<QuestionnaireResponse>>(`/questionnaires/${questionnaireId}/responses/${responseId}`)
}

// 导出问卷回复
export function exportQuestionnaireResponses(questionnaireId: number) {
  return request.get(`/questionnaires/${questionnaireId}/responses/export`, {
    responseType: 'blob'
  })
}
