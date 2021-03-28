import React, { useEffect, useState } from 'react';
import axios, { AxiosResponse } from 'axios';
import Table from './Table';
import { DocumentQuery } from '../customTypes'
import { Cookies } from 'react-cookie'
import FormDialog from './FormDialog';
import 'bootstrap/dist/css/bootstrap.min.css';
const Home = () => {
    const cookies = new Cookies();
    // const [documents, setDocuments] = useState([]);
    const [data, setData] = useState<DocumentQuery[]>([]);
    useEffect(() => {
        axios.get('http://localhost:8080/api/doc', { headers: { "Authorization": cookies.get("auth") } }).then((response) => showData(response));
    }, [])

    function showData(table: AxiosResponse) {
        console.log(table.data);
        setData(table.data);
        //console.log(fromJson[0]);
    }

    function addDocument(name: string) {
        axios.post('http://localhost:8080/api/doc', { docId: name }, { headers: { "Authorization": cookies.get("auth") } })
            .then((response: AxiosResponse) => setData((currData) => [response.data, ...currData]));

    }

    function changeUsername(name: string) {
        axios.put('http://localhost:8080/api/user/changeUsername', { newName: name }, { headers: { "Authorization": cookies.get("auth") } })
            .then((response: AxiosResponse) => console.log(response));
    }

    function deleteDocument(id: number) {
        axios.delete('http://localhost:8080/api/doc/delete/' + id, { headers: { "Authorization": cookies.get("auth") } })
            .then((response) => {
                console.log(response);
                setData((currData) => {
                    return currData.filter((el) => {
                        return el.id !== id;
                    });
                })
            })
    }

    function addEditor(email: string, docId: number) {
        console.log(email);
        console.log(docId);
        axios.post('http://localhost:8080/api/doc/editor', { email:email, docId: docId }, { headers: { "Authorization": cookies.get("auth") } })
        .then((response: AxiosResponse) => console.log(response));
    }

    return (
        <div className="container">

            <div className="row">

                <div className="col-md-2">
                    DOCUMENTS
            <hr />
                    <FormDialog buttonName={"New document"} onSubmitFunction={addDocument} />
                    <FormDialog buttonName={"Change username"} onSubmitFunction={changeUsername} />
                </div>
                <div className="col-md-10">
                    <Table data={data} deleteFunction={deleteDocument} addEditorFunction={addEditor} />
                </div>
            </div>
        </div>

    )
}

export default Home;