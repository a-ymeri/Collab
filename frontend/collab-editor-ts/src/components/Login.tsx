import axios from 'axios';
import '../style.css'
// import { useEffect, useState } from 'react';
// import { Redirect } from 'react-router';
import { Cookies } from 'react-cookie';
import React from 'react';
import Form from './Form';
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
                <div className="row">

                    <div className="col-md-6">
                        <Form propFields={["Email", "Password"]} onSubmitFunction={login} formName={"Sign in"} />
                    </div>

                    <div className="col-md-6">
                        <Form propFields={["Username", "Email", "Password"]} onSubmitFunction={register} formName={"Sign up"} />
                    </div>

                </div>
            </div>

        </div>
    )

    function login(inputs: string[]) {
        let user = {
            email: inputs[0],
            password: inputs[1]
        }

        axios.post("http://localhost:8080/login", user).then((response) => {
            console.log("auth success")
            console.log(response);
            cookies.set('auth', response.data, { maxAge: 30 * 60 });//mins*seconds
            cookies.set('email', user.email)
            window.location.replace('/')
        }).catch((error) => {
            console.log(error.response.data);
            alert("Error " + error.response.status + ": " + error.response.data);
            if (error.response.status === 401) {
                //alert(error.response);
                console.log("redirect to login with bad creds")
            }
        });

    }

    function register(inputs: string[]) {
        let user = {
            username: inputs[0],
            email: inputs[1],
            password: inputs[2]
        }

        axios.post("http://localhost:8080/api/register", user).then(() => {
            login([user.email, user.password]);
        }).catch((error) => {
            console.log(error.response.data);
            alert("Error " + error.response.status + ": " + error.response.data);
            // if (error.response.status === 401) {
            //     console.log("redirect to login with bad creds")
            // }
        });
    }
}


export default App;