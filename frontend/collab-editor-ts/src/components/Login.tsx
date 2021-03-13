import axios from 'axios';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../style.css'
// import { useEffect, useState } from 'react';
// import { Redirect } from 'react-router';
import {Cookies} from 'react-cookie';
const App = () => {
    const cookies = new Cookies();
    // axios.post('http://localhost:8080/login', user, {
    //     headers: {
    //         'Content-Type': 'form-data',
    //     }, auth: user
    // }).then((response) => { console.log(response); setData(response.data) });
    // useEffect(()=>{
    //     let bodyFormData = new FormData();
    //     bodyFormData.append("username","rag@gmail.com");
    //     bodyFormData.append("password","password");

    //     axios({
    //         method: "post",
    //         url: 'http://localhost:8080/login',
    //         data: bodyFormData,
    //         headers: {"Content-Type": "multipart/form-data"}
    //     }).then((response)=>{
    //         console.log(response)
    //     });
    // })

    return (
        <div>
            <div className="container">
                <form className="form-signin" onSubmit={(e) => { e.preventDefault(); submitData() }} >

                    <h2 className="form-signin-heading">Please sign in</h2>

                    <p>
                        <label htmlFor="username" className="sr-only">Username</label>
                        <input type="text" id="username" name="username" className="form-control" placeholder="Username" required />
                    </p>

                    <p>
                        <label htmlFor="password" className="sr-only">Password</label>
                        <input type="password" id="password" name="password" className="form-control" placeholder="Password" required />
                    </p>

                    <button className="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>

                </form>
            </div>

        </div>
    )

    function submitData() {
        let user = {
            username : (document.getElementById("username") as HTMLInputElement).value,
            password : (document.getElementById("password") as HTMLInputElement).value
        }
        
        axios.post("http://localhost:8080/login", user).then((response) => {
                console.log("auth success")
                console.log(response);
                cookies.set('auth', response.data,{maxAge:30*60});//mins*seconds
                //window.location.replace('/')
            }).catch((error)=>{
                //console.log(error.response.status) // 401
                // console.log(error.response.data.error) //Please Authenticate or whatever returned from server
                if(error.response.status===401){
                    //redirect to login
                    console.log("redirect to login with bad creds")
                }
            });
        // axios({
        //     method: "post",
        //     url: 'http://localhost:8080/login',
        //     data: bodyFormData,
        //     headers: { "Content-Type": "multipart/form-data" }
        // }).then((response) => {
        //     console.log("auth success")
        //     console.log(response);
        //     //cookies.set('auth', true,{maxAge:30*60});//mins*seconds
        //     //window.location.replace('/')
        // }).catch((error)=>{
        //     //console.log(error.response.status) // 401
        //     // console.log(error.response.data.error) //Please Authenticate or whatever returned from server
        //     if(error.response.status===401){
        //         //redirect to login
        //         console.log("redirect to login with bad creds")
        //     }
        // });
    }
}


export default App;