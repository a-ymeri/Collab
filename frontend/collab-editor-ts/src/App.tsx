import React, { useState } from 'react'
import { SyncingEditor } from './components/SyncingEditor'
import { HashRouter as Router, Switch, Route, Redirect } from 'react-router-dom';
import Home from './components/Home'
import PrivateRoute from './components/PrivateRoute';
import Login from './components/Login';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../src/style.css'
import {RichText} from './components/RichText';
// import "bootstrap/dist/css/bootstrap.css";

const App = () => {
  return (
    <Router>
      <Switch>
        
        <PrivateRoute path='/' exact component={Home} />
        <Route path = '/login' component={Login}/>
        <PrivateRoute path='/doc' component={SyncingEditor} />
        <Route path='/test' exact component={RichText} />
        <Route render={() => <Redirect to={{pathname: "/login"}} />} />

      </Switch>

    </Router>
  )
}


export default App;
