import React from 'react';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import useMediaQuery from '@material-ui/core/useMediaQuery';
import { useTheme } from '@material-ui/core/styles';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IconDefinition } from '@fortawesome/fontawesome-common-types';


interface Props {
    dialogContextText?: string,
    faButton: IconDefinition
    onSubmitFunction: Function
}
export default function ResponsiveDialog({ dialogContextText, faButton, onSubmitFunction }: Props) {
    const [open, setOpen] = React.useState(false);
    const theme = useTheme();
    const fullScreen = useMediaQuery(theme.breakpoints.down('sm'));

    const handleClickOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {

        setOpen(false);
    };

    return (
        <div style={{ display: "inline" }}>
            <FontAwesomeIcon icon={faButton} onClick={handleClickOpen}
                style={{ cursor: "pointer", color: "black", marginLeft: "1em" }} />
            <Dialog
                fullScreen={fullScreen}
                open={open}
                onClose={handleClose}
                aria-labelledby="responsive-dialog-title"
            >
                <DialogTitle id="responsive-dialog-title">{"Delete file"}</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {dialogContextText}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose} color="primary">
                        Cancel
          </Button>
                    <Button onClick={() => { handleClose(); onSubmitFunction(); }} color="primary" >
                        Confirm
          </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}
