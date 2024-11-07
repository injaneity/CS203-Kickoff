from llama_index.core import VectorStoreIndex, SimpleDirectoryReader, StorageContext
from llama_index.llms.openai import OpenAI
from pydantic import BaseModel
from dotenv import load_dotenv
import os
import openai
import chromadb
from llama_index.vector_stores.chroma import ChromaVectorStore
from fastapi import FastAPI, status
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware


load_dotenv()
openai_api_key = os.getenv('OPENAI_API_KEY')

openai.api_key = openai_api_key

# 4o-mini is really cheap, index stores vector embeddings
llm = OpenAI(model="gpt-4o-mini")
data = SimpleDirectoryReader(input_dir="data/").load_data()

# initialize client, setting path to save data
db = chromadb.PersistentClient(path="./chroma_db")
chroma_collection = db.get_or_create_collection("quickstart")

# assign chroma as the vector_store to the context
vector_store = ChromaVectorStore(chroma_collection=chroma_collection)
storage_context = StorageContext.from_defaults(vector_store=vector_store)

# take embeddings from storage context if present, else make new embeddings from data
index = VectorStoreIndex.from_documents(
    data, storage_context=storage_context
)

# context retention
chat_engine = index.as_chat_engine(
    chat_mode="condense_question", 
    llm=llm, 
    verbose=False
)

# Create the FastAPI app
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allows all origins
    allow_credentials=True,
    allow_methods=["*"],  # Allows all HTTP methods
    allow_headers=["*"],  # Allows all headers
)

class QueryModel(BaseModel):
    query: str

@app.get("/api/v1/health", status_code=status.HTTP_200_OK)
async def check_health():
    return JSONResponse(content={"status": "ok"})

@app.post("/api/v1/chatbot")
async def query_bot(query: QueryModel):

    # end chat button / key
    # btw whoever is doing the ui for this: this should be a button to trigger end of convo (close chat or something)
    if query.query.lower() == 'x':
        chat_engine.reset()
        return {"response":  "Thanks for using Kickoff AI Assistant! We hope we were able to answer your queries."} # can remove this, just for debugging / clarity
    try:
        user_query = query.query + " It's very important that you limit your response to 2 sentences and answer concisely."
        response = chat_engine.chat(user_query)
        return {"response": response.response}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))