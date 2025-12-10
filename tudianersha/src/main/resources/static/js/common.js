// API基础配置 - 使用相对路径，自动适配当前端口
const API_BASE_URL = '/api';

// 通用API请求函数
async function apiRequest(url, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
    };
    
    const config = { ...defaultOptions, ...options };
    
    try {
        const response = await fetch(`${API_BASE_URL}${url}`, config);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        // 检查响应内容类型，如果是空响应或DELETE请求，直接返回成功
        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json') || response.status === 204) {
            return { success: true, data: null };
        }
        
        // 检查响应体是否为空
        const text = await response.text();
        if (!text || text.trim() === '') {
            return { success: true, data: null };
        }
        
        // 解析JSON - 直接返回后端的ApiResponse，不再包装
        const data = JSON.parse(text);
        return data;
    } catch (error) {
        console.error('API请求错误:', error);
        return { success: false, error: error.message };
    }
}

// GET请求
async function get(url) {
    return apiRequest(url, { method: 'GET' });
}

// POST请求
async function post(url, data) {
    return apiRequest(url, {
        method: 'POST',
        body: JSON.stringify(data),
    });
}

// PUT请求
async function put(url, data) {
    return apiRequest(url, {
        method: 'PUT',
        body: JSON.stringify(data),
    });
}

// DELETE请求
async function del(url) {
    return apiRequest(url, { method: 'DELETE' });
}

// 本地存储工具
const storage = {
    // 保存数据
    set(key, value) {
        localStorage.setItem(key, JSON.stringify(value));
    },
    
    // 获取数据
    get(key) {
        const value = localStorage.getItem(key);
        return value ? JSON.parse(value) : null;
    },
    
    // 删除数据
    remove(key) {
        localStorage.removeItem(key);
    },
    
    // 清空所有数据
    clear() {
        localStorage.clear();
    }
};

// 用户相关函数
const auth = {
    // 保存当前用户
    setUser(user) {
        storage.set('currentUser', user);
    },
    
    // 获取当前用户
    getUser() {
        return storage.get('currentUser');
    },
    
    // 检查是否已登录
    isLoggedIn() {
        return this.getUser() !== null;
    },
    
    // 退出登录
    logout() {
        storage.remove('currentUser');
        window.location.href = '/login.html';
    },
    
    // 检查登录状态（页面加载时调用）
    checkAuth() {
        if (!this.isLoggedIn() && !window.location.pathname.includes('login.html') && !window.location.pathname.includes('register.html')) {
            window.location.href = '/login.html';
        }
    }
};

// 显示提示消息
function showAlert(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    
    const container = document.querySelector('.container') || document.body;
    container.insertBefore(alertDiv, container.firstChild);
    
    setTimeout(() => {
        alertDiv.remove();
    }, 3000);
}

// 显示成功消息
function showSuccess(message) {
    showAlert(message, 'success');
}

// 显示错误消息
function showError(message) {
    showAlert(message, 'error');
}

// 格式化日期
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
}

// 格式化金额
function formatMoney(amount) {
    if (amount === null || amount === undefined) return '¥0.00';
    return `¥${parseFloat(amount).toFixed(2)}`;
}

// 页面跳转
function navigate(url) {
    window.location.href = url;
}

// 获取URL参数
function getQueryParam(param) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(param);
}

// 防抖函数
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// 节流函数
function throttle(func, limit) {
    let inThrottle;
    return function() {
        const args = arguments;
        const context = this;
        if (!inThrottle) {
            func.apply(context, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// 表单验证
function validateForm(formData, rules) {
    const errors = {};
    
    for (const field in rules) {
        const value = formData[field];
        const rule = rules[field];
        
        if (rule.required && (!value || value.trim() === '')) {
            errors[field] = `${rule.label}不能为空`;
            continue;
        }
        
        if (rule.minLength && value.length < rule.minLength) {
            errors[field] = `${rule.label}至少需要${rule.minLength}个字符`;
        }
        
        if (rule.maxLength && value.length > rule.maxLength) {
            errors[field] = `${rule.label}最多${rule.maxLength}个字符`;
        }
        
        if (rule.pattern && !rule.pattern.test(value)) {
            errors[field] = `${rule.label}格式不正确`;
        }
        
        if (rule.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
            errors[field] = '邮箱格式不正确';
        }
    }
    
    return {
        isValid: Object.keys(errors).length === 0,
        errors
    };
}

// 显示表单错误
function showFormErrors(errors) {
    // 清除之前的错误提示
    document.querySelectorAll('.error-message').forEach(el => el.remove());
    
    // 显示新的错误提示
    for (const field in errors) {
        const input = document.querySelector(`[name="${field}"]`);
        if (input) {
            const errorDiv = document.createElement('div');
            errorDiv.className = 'error-message';
            errorDiv.style.color = '#dc3545';
            errorDiv.style.fontSize = '12px';
            errorDiv.style.marginTop = '5px';
            errorDiv.textContent = errors[field];
            input.parentNode.appendChild(errorDiv);
        }
    }
}

// 加载动画
function showLoading(button) {
    const originalText = button.textContent;
    button.disabled = true;
    button.innerHTML = '<span class="loading"></span> 加载中...';
    
    return {
        hide: () => {
            button.disabled = false;
            button.textContent = originalText;
        }
    };
}
