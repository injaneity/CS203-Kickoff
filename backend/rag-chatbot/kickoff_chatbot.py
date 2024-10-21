from llama_index.core import VectorStoreIndex, SimpleDirectoryReader, StorageContext
from llama_index.llms.openai import OpenAI
from dotenv import load_dotenv
import os
import openai
import chromadb
from llama_index.vector_stores.chroma import ChromaVectorStore

# llama-index-vector-stores-chroma is the pip install that causes issues -- this version of llama index doesnt auto include it
# queries not routed yet!!
def start_kickoff_chatbot():
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

    # condense_question mode for context retention
    chat_engine = index.as_chat_engine(
        chat_mode="condense_question", 
        llm=llm, 
        verbose=False
    )

    print("How can Kickoff AI Assistant help you today?")

    while True:
        user_input = input("Enter your question: ")
        query = "Qn: " + user_input + " Limit your response to 1 sentence and answer concisely."
        print()

        # end chat button / key
        if query.lower() == 'x':
            print("Thanks for using Kickoff AI Assistant! We hope we were able to answer your queries.") # can remove this, just for debugging / clarity
            break

        response = chat_engine.chat(query)
        print(response)
        print()

    chat_engine.reset()

if __name__ == "__main__":
    start_kickoff_chatbot()