import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import './App.css'
import {HTML5Backend} from "react-dnd-html5-backend";
import {DndProvider} from "react-dnd";


ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <DndProvider backend={HTML5Backend}>
            <div>
                <App/>
            </div>
        </DndProvider>
    </React.StrictMode>
    ,
)