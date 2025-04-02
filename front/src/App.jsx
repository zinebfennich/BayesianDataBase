import { useEffect, useState } from 'react'
import Axios from 'axios'


import './App.css'


function App() {
    const fetchTables = () => {
        Axios.get('http://localhost:3000/api/tables').then((res) => {
            setTables(res.data)
        })
    }

    const getTable = () => {
        var a = document.getElementById("table-id")
    }

    const [tables, setTables] = useState([])

    useEffect(() => {
        fetchTables()
    }, [])

  return (
    <>
      <div className='container'>
            <h1>React + Express + PostgreSQL</h1>
            
            <div className='items'>
                <select name="table" id="table-id" defaultValue="default">
                    <option value="default" disabled="disabled">Please select a table</option>
                    {
                        tables.map((table, index) => {
                            return(<option key={index+1} value={table.tablename}>{table.tablename}</option>)
                        })
                    }
                </select>
                <button className='btn' onClick={() => {getTable()}}>submit</button>
            </div>
      </div>
    </>
  )
}

export default App
