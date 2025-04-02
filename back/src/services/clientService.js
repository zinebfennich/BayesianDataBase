import { query } from '../db.js'


export const getTables = async() => {
    const { rows } = await query("SELECT * FROM pg_catalog.pg_tables WHERE schemaname='public'")

    return rows
}

export const getTable = async(table) => {
    const { rows } = await query(`SELECT * FROM ${table}`)

    return rows
}