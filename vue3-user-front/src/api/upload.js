import request from '@/utils/request'

/**
 * 文件上传相关API
 */

// 单文件上传
export function uploadSingle(file, moduleName, businessType = 'default') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('moduleName', moduleName)
  formData.append('businessType', businessType)
  
  return request.post('/file/upload/single', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 批量文件上传
export function uploadBatch(files, moduleName, businessType = 'default') {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  formData.append('moduleName', moduleName)
  formData.append('businessType', businessType)
  
  return request.post('/file/upload/batch', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
} 