import React, { useEffect, useState } from 'react' 
import clsx from 'clsx'
import Axios from 'axios'

import styles from "./Bayesian.module.css"


const Bayesian = () => {
    const fetchTables = () => {
        Axios.get('http://localhost:3000/api/tables').then((res) => {
            setTables(res.data)
            console.log(res.data)
        })
    }

    const getTable = () => {
        var a = document.getElementById("table-id")

        console.log(a.value)
    }

    const [tables, setTables] = useState([])

    useEffect(() => {
        fetchTables()
    }, [])

  return (
    <>
        <h2 className='title'>Analyse</h2>
        
        <div className={styles.items}>
            <select className={styles.select} name="table" id="table-id" defaultValue="default">
                <option value="default" disabled="disabled">Please select a table</option>
                {
                    tables.map((table, index) => (
                        <option key={index+1} value={table.tablename}>{table.tablename}</option>
                    ))
                }
            </select>
            <button className={clsx("btn", styles.submit_btn)} onClick={() => {getTable()}}>submit</button>
        </div>

        <h2 className={clsx("title", styles.result)}>Result</h2>
        <div>
            <p>Waiting for a response...</p>
        </div>

    </>
  )
}

export default Bayesian