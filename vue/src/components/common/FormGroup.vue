<template>
  <div class="form-group">
    <label v-if="label" :for="id">{{ label }}<span v-if="required" class="required">*</span></label>
    
    <input 
      v-if="type !== 'textarea' && type !== 'select'"
      :id="id"
      :type="type"
      :value="modelValue"
      :required="required" 
      :disabled="disabled"
      :placeholder="placeholder"
      class="form-control"
      @input="updateValue"
    />
    
    <textarea 
      v-else-if="type === 'textarea'" 
      :id="id"
      :value="modelValue"
      :required="required"
      :disabled="disabled"
      :placeholder="placeholder"
      class="form-control"
      @input="updateValue"
    ></textarea>
    
    <select 
      v-else-if="type === 'select'" 
      :id="id"
      :value="modelValue"
      :required="required"
      :disabled="disabled"
      class="form-control"
      @change="updateValue"
    >
      <option v-if="placeholder" value="" disabled>{{ placeholder }}</option>
      <option 
        v-for="option in options" 
        :key="option.value" 
        :value="option.value"
      >
        {{ option.label }}
      </option>
    </select>
    
    <small v-if="helpText" class="help-text">{{ helpText }}</small>
    <small v-if="error" class="error-text">{{ error }}</small>
  </div>
</template>

<script>
export default {
  name: 'FormGroup',
  props: {
    id: {
      type: String,
      required: true
    },
    label: {
      type: String,
      default: ''
    },
    modelValue: {
      type: [String, Number, Boolean],
      default: ''
    },
    type: {
      type: String,
      default: 'text'
    },
    required: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    placeholder: {
      type: String,
      default: ''
    },
    helpText: {
      type: String,
      default: ''
    },
    error: {
      type: String,
      default: ''
    },
    options: {
      type: Array,
      default: () => []
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const updateValue = (event) => {
      emit('update:modelValue', event.target.value);
    };
    
    return {
      updateValue
    };
  }
};
</script>

<style scoped>
.form-group {
  margin-bottom: 16px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.required {
  color: #e53935;
  margin-left: 4px;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

.form-control:focus {
  border-color: #42b983;
  outline: 0;
  box-shadow: 0 0 0 0.2rem rgba(66, 185, 131, 0.25);
}

.form-control:disabled {
  background-color: #f5f5f5;
  opacity: 0.7;
  cursor: not-allowed;
}

textarea.form-control {
  min-height: 100px;
  resize: vertical;
}

select.form-control {
  appearance: none;
  padding-right: 24px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='4' viewBox='0 0 8 4'%3E%3Cpath fill='%23666' d='M0 0l4 4 4-4z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 8px 4px;
}

.help-text {
  display: block;
  margin-top: 4px;
  color: #666;
  font-size: 12px;
}

.error-text {
  display: block;
  margin-top: 4px;
  color: #e53935;
  font-size: 12px;
}
</style> 