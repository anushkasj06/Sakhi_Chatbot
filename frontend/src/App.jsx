import { useState } from 'react'
import heroImg from './assets/hero.png'
import './App.css'

function App() {

  return (
    <>
      <div>
        <br/>
        <h1>Warehouse Management System</h1>
        <br/>
        <p>Welcome to the Warehouse Management System! This application allows you to manage inventory, track shipments, and optimize warehouse operations efficiently.</p>
        <br/><br/>
        <img src={heroImg} alt="Warehouse Management" className="hero-image" />
        <br/><br/><br/>
        <p>Comming Soon...</p>
      </div>
    </>
  )
}

export default App
