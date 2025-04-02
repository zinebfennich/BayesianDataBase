import * as tableService from "../services/clientService.js"

export const getTables = async (req, res) => {
    try{
        const tables = await tableService.getTables()

        res.status(200).json(tables)
    }
    catch(err){
        console.error("Error fetching tables: ", err)
        res.status(500).json({message: 'Internal Server Error'})
    }
}

/* export const getTable = async (req, res) => {
    try{
        const table = await tableService.getTable(arg)

        res.status(200).json(table)
    }
    catch(err){
        console.error("Error fetching table: ", err)
        res.status(500).json({message: 'Internal Server Error'})
    }
} */