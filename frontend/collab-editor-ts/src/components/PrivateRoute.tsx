import React from 'react';
import {Route, Redirect} from 'react-router-dom';
import {Cookies} from 'react-cookie';
interface Props{
  component:any,
  path:string,
  exact?:boolean
}
export default function PrivateRoute({component: Component, ...rest} : Props) {
  const cookies = new Cookies();
  let authed = cookies.get("auth") ? true : false; //cookies.get("auth") === "true";// ? true : false;  
  // console.log(typeof cookies.get("auth"));
  // console.log(authed);
  return (
    <Route
      {...rest}
      render={(props) => authed === true
        ? <Component />
        : <Redirect to={{pathname: '/login', state: {from: props.location}}} />}
    />
  )
}