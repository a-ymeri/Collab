import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import { DocumentQuery } from '../customTypes';
import { Link } from 'react-router-dom'
import { faTrash, faUserPlus } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import FormDialog from './FormDialog';
import ConfirmDialog from './ConfirmDialog';
// import axios from 'axios';
// import { Cookies } from 'react-cookie'
const useStyles = makeStyles({
  table: {
    minWidth: 650,
  },
});

let rows: DocumentQuery[] = [
  // createData('Frozen yoghurt', 159, 6.0, 24, 4.0),
  // createData('Ice cream sandwich', 237, 9.0, 37, 4.3),
  // createData('Eclair', 262, 16.0, 24, 6.0),
  // createData('Cupcake', 305, 3.7, 67, 4.3),
  // createData('Gingerbread', 356, 16.0, 49, 3.9),
];

interface Props {
  data: DocumentQuery[],
  deleteFunction: Function,
  addEditorFunction: Function
}

export default function BasicTable({ data, deleteFunction, addEditorFunction }: Props) {
  const classes = useStyles();

  // const cookies = new Cookies();
  rows = data ? data : [];


  return (
    <TableContainer component={Paper}>
      <Table className={classes.table} aria-label="simple table">
        <TableHead>
          <TableRow>
            <TableCell align="left">Name</TableCell>
            <TableCell align="left">Owner</TableCell>
            <TableCell align="left">Last Modified</TableCell>
            <TableCell align="left">Actions</TableCell>
            {/* <TableCell align="right">File Size</TableCell> */}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row: DocumentQuery, index) => (

            <TableRow key={row.name + index}>

              {/* <TableCell component="th" scope="row">
                {row.name}
              </TableCell> */}
              <TableCell align="left"><Link to={"/doc/" + row.id}>{row.name} </Link></TableCell>
              <TableCell align="left">{row.owner}</TableCell>
              <TableCell align="left">{row.lastModified}</TableCell>
              <TableCell align="left">
                {/* <FontAwesomeIcon icon={faTrash} onClick={() => deleteFunction(row.id)}
                  style={{ cursor: "pointer", color: "red" }} /> */}


                <ConfirmDialog dialogContextText ={"Are you sure you want to delete this file?"} faButton={faTrash} onSubmitFunction={() => deleteFunction(row.id)}/>
                {/* FORM DIALOG WITH AN FA BUTTON IN IT, THAT WHEN CLICKED AND SUBMITTED WILL ADD A USER */}
                <FormDialog faButton={faUserPlus} onSubmitFunction={(email: string) => addEditorFunction(email, row.id)} />
              </TableCell>
              {/* <TableCell align="right">{row.file_size}</TableCell> */}

            </TableRow>

          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
