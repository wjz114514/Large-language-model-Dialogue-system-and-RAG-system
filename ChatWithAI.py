import requests
import os
from langchain_core.prompts import PromptTemplate
from langchain_openai import ChatOpenAI
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.document_loaders import PyPDFLoader
from langchain_community.vectorstores import Qdrant
from langchain.retrievers.multi_query import MultiQueryRetriever
from langchain.chains import RetrievalQA
from langchain_community.embeddings import DashScopeEmbeddings
from langchain_core.output_parsers import StrOutputParser
import json
import tkinter as tk
from tkinter import ttk
from tkinter import filedialog
from tkinter import END

os.environ["OPENAI_API_KEY"] = "sk-b03c3c5d7afe42afbf92f1f9af91b016"  # dashscope API Key
os.environ["OPENAI_API_BASE"] = "https://dashscope.aliyuncs.com/compatible-mode/v1"  # DashScope base_url
global global_knowledge_base_path
def chat_with_llama3(output_box,input_question):
    url = 'http://127.0.0.1:11434/api/generate'
    headers = {'Content-Type': 'application/json'}
    payload = {
        'model': "llama3",
        'prompt': "请用中文回答： " + input_question,
    }
    response = requests.post(url, json=payload, headers=headers, stream=True)
    for line in response.iter_lines():
        if line:
            data = json.loads(line)
            output_box.insert(END, data['response'])
            output_box.update()
            if data['done']:
                output_box.insert(END, "\n\n")
                break

def chat_with_qwen(output_box,input_question):
    llm = ChatOpenAI(model="qwen1.5-14b-chat")
    prompt = PromptTemplate.from_template("{question}")
    question = input_question
    chain = prompt | llm
    str_chain = chain | StrOutputParser()
    str_chain.invoke({"question": question})
    for chunk in str_chain.stream({"question": question}):
        output_box.insert(END, chunk)
        output_box.update()
    output_box.insert(END, "\n\n")


def chat_with_RAG(input_query):
    # 加载Documents
    base_dir = global_knowledge_base_path
    documents = []
    for file in os.listdir(base_dir):
        file_path = os.path.join(base_dir, file)
        if file.endswith('.pdf'):
            loader = PyPDFLoader(file_path)
            documents.extend(loader.load())

    text_splitter = RecursiveCharacterTextSplitter(chunk_size=200, chunk_overlap=50)
    chunked_documents = text_splitter.split_documents(documents)

    # 创建 embeddings
    embeddings = DashScopeEmbeddings(
       model="text-embedding-v1", dashscope_api_key="sk-b03c3c5d7afe42afbf92f1f9af91b016"
    )

    # 加载文档到向量数据库
    vectorstore = Qdrant.from_documents(
        documents=chunked_documents,
        embedding=embeddings,
        location=":memory:",
        collection_name="documents"
    )

    # 构建一个MultiQueryRetriever
    retriever_from_llm = MultiQueryRetriever.from_llm(retriever=vectorstore.as_retriever(), llm=ChatOpenAI(model="qwen1.5-14b-chat"))
    # 实例化一个RetrievalQA链
    qa_chain = RetrievalQA.from_chain_type(llm=ChatOpenAI(model="qwen1.5-14b-chat"), retriever=retriever_from_llm)
    result = qa_chain.invoke({"query": input_query})
    return result['result']

def UI():
    root = tk.Tk()
    root.title("ChatWithAI")
    root.configure(bg='lightblue')  # 设置窗口的背景颜色

    # 设置窗口大小
    screen_width = root.winfo_screenwidth()
    screen_height = root.winfo_screenheight()
    window_width = int(screen_width / 2)
    window_height = int(screen_height / 2)
    # 设置窗口位置为屏幕中心
    x = (screen_width - window_width) // 2
    y = (screen_height - window_height) // 2
    root.geometry(f"{window_width}x{window_height}+{x}+{y}")

    # 创建输出框
    output_box = tk.Text(root, height=30, width=60)
    output_box.pack(side=tk.LEFT, padx=5, pady=10)
    output_box.configure(bg='lightgrey')
    # 创建下拉框
    combo_box = ttk.Combobox(root, width=12)
    combo_box.pack(side=tk.LEFT)
    combo_box['values'] = ('与Llama3对话', '与千问对话', '与RAG系统对话')
    combo_box.current(0)  # 设置默认选项
    # 创建输入框
    input_box = tk.Text(root, height=3, width=27)
    input_box.pack(side=tk.LEFT, padx=(5, 5))
    input_box.configure(bg='lightgrey')

    def execute_function():
        input_question = input_box.get(1.0, "end-1c")  # 获取输入框中的文本
        selected_function = combo_box.get()
        output_box.insert(tk.END, f"用户：{input_question}\n")  # 将用户输入插入到输出框中
        output_box.update()
        if selected_function == '与Llama3对话':
            output_box.insert(tk.END, "llama3:")
            output_box.update()
            chat_with_llama3(output_box,input_question)
        elif selected_function == '与千问对话':
            output_box.insert(tk.END, "千问：")
            output_box.update()
            chat_with_qwen(output_box,input_question)
        elif selected_function == '与RAG系统对话':
            output = chat_with_RAG(input_question)
            output_box.insert(tk.END, "RAG系统：")
            output_box.update()
            output_box.insert(tk.END, output)  # 插入新的输出
            output_box.insert(tk.END, "\n\n")
        input_box.delete(1.0, tk.END)

    submit_button = tk.Button(root, text="发送", command=execute_function, bg="black", fg="white")
    submit_button.pack(side=tk.LEFT)

    def select_knowledge_base():
        global global_knowledge_base_path
        global_knowledge_base_path = filedialog.askdirectory()
        # knowledge_base_path 将会是用户选择的文件夹路径
        if global_knowledge_base_path:
            output_box.insert(tk.END, f"知识库路径：{global_knowledge_base_path}\n\n")

    select_kb_button = tk.Button(root, text="选择知识库", command=select_knowledge_base, width=20, bg="black", fg="white")
    select_kb_button.pack(side=tk.BOTTOM, anchor=tk.SW, pady=10)

    root.mainloop() # 主循环

if __name__ == "__main__":
    UI()