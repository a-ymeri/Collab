import React, { useEffect, useState } from 'react';
import axios, { AxiosResponse } from 'axios';
import Table from './Table';
import {DocumentQuery} from '../customTypes'
import {Cookies} from 'react-cookie'
const Home = () => {
    const cookies = new Cookies();
    // const [documents, setDocuments] = useState([]);
    const [data, setData] = useState<DocumentQuery[]>([]);
    useEffect(()=>{
        axios.get('http://localhost:8080/api/doc', {headers:{ 'Access-Control-Allow-Origin': '*', "Authorization":cookies.get("auth")}}).then((response)=>showData(response));
    },[])
   
    function showData( table:AxiosResponse ){
        setData(table.data);
        //console.log(fromJson[0]);
    }

    return (
        <div>
            DOCUMENTS
            <Table data={data} />
        </div>

    )
}

export default Home;