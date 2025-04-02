import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { links } from './constants'

import Home from "./pages/Home/Home"
import NoPage from "./pages/NoPage"

import Layout from './components/Layout/Layout'


function App() { 
    console.log(links.dashboard)
    
  return (
    <>
      <Router>
        <Routes>
            {/* Home page */}
            <Route index element={<Home/>}/>

            {/* App = /dashboard */}
            <Route element={<Layout/>}>
                {Object.values(links)
                .filter((element) => element.where === 'app')
                .map((element, id) => (
                    <Route
                        caseSensitive
                        key={id}
                        path={element.path}
                        element={typeof element.page !== 'undefined' ? element.page : <NoPage />}
                    />
                ))
            }
            </Route>
        
            {/* Default page */}
            <Route path='*' element={<NoPage/>}/>
        </Routes>
      </Router>
    </>
  )
}

export default App
