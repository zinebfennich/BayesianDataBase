import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

import './style/global.css'
import './style/modern-normalize.css'

import App from './App.jsx'

import './style/utils.css'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
