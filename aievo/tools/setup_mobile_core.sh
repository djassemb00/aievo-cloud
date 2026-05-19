#!/data/data/com.termux/files/usr/bin/bash

echo "🚀 البدء في تحويل الهاتف إلى نواة AIEVO السيادية..."

# 1. تحديث المستودعات وتثبيت الأدوات الأساسية
pkg update && pkg upgrade -y
pkg install python git clang make cmake -y

# 2. إنشاء بيئة العمل
mkdir -p ~/aievo_core
cd ~/aievo_core

# 3. تثبيت المكتبات البرمجية المطلوبة
pip install --upgrade pip
pip install flask flask-cors langchain chromadb pydantic python-dotenv

# 4. تثبيت محرك التفكير المحلي (Llama-cpp للموبايل)
echo "🧠 تثبيت محرك التفكير المحلي (قد يستغرق وقتاً)..."
pip install llama-cpp-python

# 5. تنزيل نموذج ذكاء اصطناعي خفيف ومناسب لـ 8GB RAM
# سنستخدم نموذج Gemma-2b بنسخة GGUF لضمان السرعة
mkdir -p ~/aievo_core/models
echo "📥 سيتم تنزيل نموذج الذكاء الاصطناعي لاحقاً..."

echo "✅ تم إعداد البيئة الأساسية للنواة على هاتفك."
echo "انقل ملفات المشروع الآن إلى مجلد ~/aievo_core"
