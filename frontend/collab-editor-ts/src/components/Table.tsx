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
import {Link} from 'react-router-dom'
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
  data: DocumentQuery[]
}

export default function BasicTable({ data }: Props) {
  const classes = useStyles();
  console.log(data.length);

  console.log(data);
  rows = data ? data : [];


  return (
    <TableContainer component={Paper}>
      <Table className={classes.table} aria-label="simple table">
        <TableHead>
          <TableRow>
            <TableCell align="right">Name</TableCell>
            <TableCell align="right">Owner</TableCell>
            <TableCell align="right">Last Modified</TableCell>
            <TableCell align="right">File Size</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row: DocumentQuery, index) => (
            
              <TableRow key={row.name + index}>
                
                {/* <TableCell component="th" scope="row">
                {row.name}
              </TableCell> */}
                <TableCell align="right"><Link to={"/doc/"+row.id}>{row.name} </Link></TableCell>
                <TableCell align="right">{row.owner}</TableCell>
                <TableCell align="right">{row.last_modified}</TableCell>
                <TableCell align="right">{row.file_size}</TableCell>
                
              </TableRow>
            
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
