import express from "express"
import cors from 'cors'
import clientRoutes from "./routes/clientRoute.js"


const app = express()
const port = 3000


app.use(cors())
app.use(express.json())
app.use('/api', clientRoutes)

app.get('/', (req, res) => {
    res.send('<p>hello</p>')
})

app.listen(port, () => {
    console.log("expressssssssssssss from 3000")
})