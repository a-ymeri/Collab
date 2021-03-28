import React from 'react';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { IconDefinition } from '@fortawesome/fontawesome-common-types';

interface Props {
    buttonName?: string,
    onSubmitFunction: Function,
    faButton?: IconDefinition,
    noInputField?: boolean
}

export default function FormDialog({ buttonName, onSubmitFunction, faButton, noInputField }: Props) {
    const [open, setOpen] = React.useState(false);

    const handleClickOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };

    return (
        <div style={{ display: 'inline' }}>
            { faButton
                ?
                <FontAwesomeIcon icon={faButton} onClick={handleClickOpen}
                    style={{ cursor: "pointer", color: "black", marginLeft: "1em" }} />
                :
                <Button variant="outlined" color="primary" onClick={handleClickOpen}>
                    {buttonName}
                </Button>

            }
            <Dialog open={open} onClose={handleClose} aria-labelledby="form-dialog-title" fullWidth maxWidth="xs">
                <DialogTitle id="form-dialog-title">{buttonName}</DialogTitle>
                <DialogContent>
                    {
                        noInputField ?
                            null
                            :
                            <TextField
                                autoFocus
                                margin="dense"
                                id="documentName"
                                label="Document Name"
                                type="text"
                                fullWidth

                            />
                    }
                </DialogContent>
                <DialogActions>
                    <Button style={{

                    }} onClick={handleClose}>
                        Cancel
                    </Button>
                    <Button style={{
                        backgroundColor: "#4285f4",
                        color: "#fff",
                    }} onClick={() => {
                        onSubmitFunction((document.getElementById("documentName") as any)?.value);
                        handleClose()
                    }}>
                        Confirm
                        </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}
