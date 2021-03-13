import React, { useState } from 'react'
import { SyncingEditor } from './components/SyncingEditor'
import { HashRouter as Router, Switch, Route, Redirect } from 'react-router-dom';
import Home from './components/Home'
import PrivateRoute from './components/PrivateRoute';
import Login from './components/Login';
const App = () => {
  return (
    <Router>
      <Switch>
        
        <PrivateRoute path='/' exact component={Home} />
        <Route path = '/login' component={Login}/>
        <PrivateRoute path='/doc' component={SyncingEditor} />
        <Route render={() => <Redirect to={{pathname: "/login"}} />} />
      </Switch>

    </Router>
  )
}


export default App;
