package com.erpnext.pos.remoteSource.dto

data class CategoryList(val categories: List<CategoryDto>)
data class CategoryDto(val name: String)