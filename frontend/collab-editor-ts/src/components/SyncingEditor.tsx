import React, { useEffect, useMemo, useRef, useState } from 'react'
import { createEditor, InsertTextOperation, Operation } from 'slate'
import { Slate, Editable, withReact } from 'slate-react'

import * as Stomp from "stompjs";
import SockJS from "sockjs-client";
import { RemoveTextOperation } from 'slate';
interface Props { }


interface OperationKey {
    siteID: number,
    stateID: number
}

type Identifier = {
    siteID: number,
    stateID: number
}


type CustomOperation = (InsertTextOperation | RemoveTextOperation) & Identifier;


//TODO: Put inside of component
let stateID: number = 0.0;
//TODO: get ID from the back, also UUID
const ID = Date.now();
console.log(ID);

let effectsRelation = new Map<OperationKey, OperationKey>();

let historyBuffer: CustomOperation[] = []; // List of changes that have been recorded, potentially useful for ETSOS
let toSendBuffer: CustomOperation[] = []; // List of changes TO BE SENT, constantly altered by changes before ACK
let concurrentChanges: CustomOperation[] = []; //List of changes to be sent, unaltered by changes before ACK, used for transformation

let received: CustomOperation[] = [];
export const SyncingEditor: React.FC<Props> = () => {

    const socket = useRef(new SockJS('http://localhost:8080/collab-editor'))
    const stompClient = useRef(Stomp.over(socket.current));

    const editor = useMemo(() => withReact(createEditor()), [])

    //const [historyBuffer, setHistoryBuffer] = useState([] as CustomOperation[]);

    const remote = useRef(false);
    const sending = useRef(false);
    // Add the initial value when setting up our state.
    const [value, setValue] = useState([
        {
            type: 'paragraph',
            children: [{ text: '' }],
        },
    ])

    useEffect(() => {
        if (!stompClient.current.connected) {
            stompClient.current.connect({}, function () {
                let id = window.location.href.split('/').pop();
                stompClient.current.subscribe('/app/file/'+id, function (data: Stomp.Message) {
                    let response = JSON.parse(data.body).body;

                    setValue([
                        {
                            type: 'paragraph',
                            children: [{ text: response.text }]
                        },
                    ]);
                    stateID = Number(response.state);
                });

                stompClient.current.subscribe('/topic/1', function (data: any) {
                    let op: CustomOperation = JSON.parse(data.body);
                    onReceived(op);
                    received.push(op);
                });

            })
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    function onReceived(op: CustomOperation) {
        if (op.siteID !== ID) {
            console.log("Remote execution of: character:" + op.text + " | offset:" + op.offset + " | stateID: " + op.stateID);
            let tempOp: CustomOperation;

            //If it's below 0, useless operation
            if (op.offset >= 0) {
                for (let i = 0; i < toSendBuffer.length; i++) {
                    tempOp = copy(op);
                    op = inclusionTransform(op, toSendBuffer[i]);
                    if (op.offset === -1) {
                        toSendBuffer.splice(i, 1);
                        break;
                    }
                    toSendBuffer[i] = inclusionTransform(toSendBuffer[i], tempOp);

                }
            }

            toSendBuffer.forEach((operation, index) => {
                toSendBuffer[index].stateID++;
            })
            
            if (op.offset >= 0) {
                remote.current = true;

                editor.apply(op);
                historyBuffer.push(op)
                //setHistoryBuffer(hb => [...hb, op]);
                remote.current = false;
                stateID++;
                console.log("Remote execution of: character:" + op.text + " | offset:" + op.offset + " | stateID: " + op.stateID);
            }
        } else {
            //Ack received
            toSendBuffer.forEach((operation, index) => {
                toSendBuffer[index].stateID++;
            })
            toSendBuffer.splice(0, 1);
            concurrentChanges.splice(0, 1);
            sending.current = false;
            if (toSendBuffer.length > 0) {
                sendCharacter(toSendBuffer[0] as CustomOperation)
            }
            stateID++;
            historyBuffer.push(op);
        }
    }

    useEffect(() => {
        console.log(value[0].children[0].text);
    }, [value])

    function sendCharacter(operation: Operation) {
        //stompClient.send("/app/sendcharacter", {}, JSON.stringify({type: operation.type, character: operation.text, index: operation.offset, id: ID, stateID: stateID}));
        if (!sending.current) {
            sending.current = true;

            setTimeout(() => {
                stompClient.current.send("/app/sendcharacter", {}, JSON.stringify(operation))
            }, 0); //was setTimeout TODO: remove

        }
    }

    return (
        <div>
            <Slate
                editor={editor}
                value={value}
                onChange={newValue => {
                    setValue(newValue as any);

                    // const ops = newValue.operations.filter()
                    const ops = editor.operations.filter(o => {
                        if (o) { //not undefined
                            return (
                                o.type === "remove_text" ||
                                o.type === "insert_text"
                            );
                        }
                        return false;
                    })//.map((o) => ({ ...o, data: { id: ID, stateID: stateID } })); // instead of one, get some unique identifier

                    ops.forEach((op) => {
                        if (!op.siteID) {
                            op.siteID = ID;
                            op.stateID = stateID;
                            toSendBuffer.push(op as CustomOperation);
                            concurrentChanges.push(op as CustomOperation);
                            console.log("Local change:" + value[0].children[0].text)
                            console.log("offset: " + op.offset + "| character: " + op.text + "| stateID: " + op.stateID)
                            //TODO: uncomment
                            sendCharacter(op);
                        }

                    });
                }}
            >
                <Editable
                    onKeyDown={event => {

                    }} />
            </Slate>
            <button onClick={() => {
                if (toSendBuffer.length > 0)
                    sendCharacter(toSendBuffer[0] as CustomOperation)
            }
            }>Send</button>
            <button onClick={() => {
                if (received.length > 0)
                    onReceived(received.splice(0, 1)[0])
            }}>Receive</button>
        </div>
    )


    function inclusionTransform(o1: CustomOperation, o2: CustomOperation) {
        /*-1 = O1 is to the left of O2, so don't transform
         * 0 = Same position, used for double deletions
         * 1 = O1 is to the right of O2, transform*/

        //TODO: Uncomment
        let relationship: number = get_ER_IT(o1, o2);

        // Clone o1 into newOp1 so we can modify newOp1 without affecting o1
        let newOp1: CustomOperation = copy(o1);

        if (relationship === 0) { // Same position, double deletion
            newOp1.offset = -1;// position = -1 -> Don't delete, identity operation
        } else {
            if (relationship === 1) { // o2 is to the left of o1
                if (o2.type === "insert_text") {// ins = insertion operator
                    newOp1.offset++; // position++
                } else { // o2.type = deletion
                    newOp1.offset--;// position--
                }
            }
        }
        return newOp1;
    }

    function get_ER_IT(o1: CustomOperation, o2: CustomOperation) {

        // Check if there is a mapping of o1->o2 or o2->o1
        let o1Key = { siteID: o1.siteID as number, stateID: o1.stateID as number };
        let o2Key = { siteID: o2.siteID as number, stateID: o2.stateID as number };
        if (effectsRelation.get(o1Key) === o2Key) {
            return -1;
        }
        if (effectsRelation.get(o2Key) === o1Key) {
            return 1;
        }

        let relationship = 1;

        // If no existing relationship, create a new one
        if (o1.offset < o2.offset) {
            relationship = -1; // I.e. don't transform
            effectsRelation.set(o1Key, o2Key);
        } else if (o1.offset === o2.offset) {
            // If two insertions, arbitrarily choose by site id, don't transform
            if (o1.type === "insert_text" && o2.type === "insert_text" && o1.siteID < o2.siteID) {
                relationship = -1;
                effectsRelation.set(o1Key, o2Key);
            } else if (o1.type === "remove_text" && o2.type === "remove_text") {
                relationship = 0; // Delete only once
            } else if (o1.type === "insert_text" && o2.type === "remove_text") {
                relationship = -1;
                effectsRelation.set(o1Key, o2Key);
            }
        }
        return relationship;
    }





  /*
    TRANSFORMATION FUNCTIONS
    MAYBE I PUT THESE IN ANOTHER FILE
    MAYBE I DON'T
    WILL SEE
    */


    //TODO: Re-evaluate the need for happened, etsoshappened etc.
    // function integrate(op: CustomOperation, happened: CustomOperation[], concurrent: CustomOperation[]) {
    //     // Normally this should be a clear split anyway but it also contextually
    //     // serializes if needed
    //     // Will have to rethink the necessity of the serial contextualization but it
    //     // doesn't hurt the performance much


    //     //for now, gonna comment these, TODO: revisir
    //     // let transposedLists = transposePreCon(op, sequence);
    //     // let happened: CustomOperation[] = transposedLists[0];
    //     // let concurrent: CustomOperation[] = transposedLists[1];


    //     let newOp: CustomOperation;

    //     // If no concurrent operations, just return and dont transform
    //     if (concurrent.length === 0) {

    //         newOp = copy(op);
    //         return newOp;
    //     }

    //     if (op.type === "remove_text") {
    //         newOp = it_sq(op, concurrent);
    //         return newOp;
    //     } else { // op.type == "ins"

    //         // fixed list
    //         let etsos_happened: CustomOperation[] = buildETSOS(happened);

    //         // insertions and deletions that have happened
    //         let insDelLists = transposeInsDel(etsos_happened);
    //         // ArrayList<Operation> happenedInsert = insDelLists.get(0);
    //         let happenedDelete: CustomOperation[] = insDelLists[1];

    //         // all deletions that have happened and concurrent operations
    //         let newList: CustomOperation[] = happenedDelete.concat(concurrent);
    //         let happenedDelConc: CustomOperation[] = buildETSOS(newList);

    //         // All insertions and deletions
    //         let insDels = transposeInsDel(happenedDelConc);
    //         let insertions: CustomOperation[] = insDels[0];
    //         //console.log(insertions.join());
    //         let deletions: CustomOperation[] = insDels[1];
    //         //console.log(deletions.join())

    //         let innerOp: CustomOperation = et_sq(op, happenedDelete); // o'', this is the back backwards

    //         let allInsDels: CustomOperation[] = insertions.concat(deletions);
    //         newOp = it_sq(innerOp, allInsDels);
    //         return newOp;
    //     }
    // }


    // function exclusionTransform(o2: CustomOperation, o1: CustomOperation) {
    //     let relationship: number = get_ER_ET(o1, o2);
    //     let newOp2: CustomOperation = copy(o2);
    //     if (relationship === 0) {
    //         newOp2.offset = -1;
    //         // throw new Exception("HALT, DOUBLE DELETION");
    //     } else {
    //         newOp2 = copy(o2);

    //         if (relationship < -1) {// o1 precedes o2 so transform o2
    //             if (o1.type === "insert_text") {
    //                 newOp2.offset--; // Shift index to the left by 1
    //             } else { // o1.type = deletion
    //                 newOp2.offset++;// Shift index to the right by 1
    //             }
    //         }
    //     }
    //     return newOp2;
    // }

    // function get_ER_ET(o1: CustomOperation, o2: CustomOperation) {
    //     let o1Key = { siteID: o1.siteID as number, stateID: o1.stateID as number };
    //     let o2Key = { siteID: o2.siteID as number, stateID: o2.stateID as number };
    //     if (effectsRelation.get(o1Key) === o2Key) {
    //         return -1;
    //     }
    //     if (effectsRelation.get(o2Key) === o1Key) {
    //         return 1;
    //     }

    //     let relationship: number;

    //     if (o1.offset < o2.offset) {
    //         relationship = -1;
    //     } else if (o1.offset > o2.offset) {
    //         relationship = 1;
    //     } else { // o1.pos == o2.pos
    //         if (o1.type === "insert_text" && o2.type === "insert_text") {
    //             relationship = 1;
    //         } else if (o1.type === "remove_text" && o2.type === "remove_text") {
    //             relationship = -1;
    //         } else if (o1.type === "remove_text" && o2.type === "insert_text") {
    //             relationship = 1;
    //         } else { // o1.type = ins, o2.type = del
    //             relationship = 0;
    //         }
    //     }

    //     // record in ER
    //     switch (relationship) {
    //         case -1:
    //             effectsRelation.set(o1Key, o2Key);
    //             break;
    //         case 1:
    //             effectsRelation.set(o2Key, o1Key);
    //             break;
    //     }

    //     return relationship;
    // }

    // /*
    //  * Precondition: Sequence must be IT-safe (i.e. all insertion operations should
    //  * be before deletion operations
    //  */
    // function it_sq(op: CustomOperation, sequence: CustomOperation[]) {
    //     let newOp: CustomOperation = copy(op); // Copy the object
    //     for (let i = 0; i < sequence.length; i++) {
    //         newOp = inclusionTransform(newOp, sequence[i]);
    //         if (newOp.offset < 0) { //identity operation
    //             break;
    //         }
    //     }
    //     return newOp;
    // }


    // /*
    //  * Precondition: Sequence must be ET-safe (i.e. for any two i,j where i<j,
    //  * either pos(i)<pos(j) or pos(i)=pos(j) but j deletes i's insertion
    //  */
    // function et_sq(op: CustomOperation, sequence: CustomOperation[]) {
    //     let newOp: CustomOperation = copy(op); // Copy the object
    //     let i = 0;

    //     // Since our array is sorted by Effects Relation, this first for-loop removes
    //     // all characters
    //     // which do not affect op because they are on the right of it. Possible
    //     // optimisation by not calling ET?
    //     // Double check in get_er_et?
    //     for (i = sequence.length - 1; i >= 0; i--) {
    //         newOp = exclusionTransform(newOp, sequence[i]);
    //         if (get_ER_ET(sequence[i], newOp) === -1) {
    //             break;
    //         }
    //     }

    //     // The remaining elements from the last array do affect op
    //     for (let j = i - 1; j >= 0; i--) {
    //         if (sequence[j].type === "insert_text") {
    //             newOp.offset--;
    //         } else {
    //             newOp.offset++;
    //         }
    //     }
    //     return newOp;
    // }

    // function transpose(o2: CustomOperation, o1: CustomOperation) {
    //     let transposedOperations: CustomOperation[] = [];

    //     if (get_ER_ET(o1, o2) === 0) {// Same character, don't transpose, return as is
    //         //arr.splice(index, 0, item);
    //         transposedOperations.push(o1)
    //         transposedOperations.push(o2);
    //     } else {
    //         transposedOperations.push(exclusionTransform(o1, o2));
    //         transposedOperations.push(inclusionTransform(o2, transposedOperations[0]));
    //     }

    //     return transposedOperations;
    // }


    // function transposeOSq(sq: CustomOperation[], op: CustomOperation) {

    //     let newOp = copy(op); // Clone op into newOp
    //     let newSq: CustomOperation[] = [];

    //     // Clone the sequence
    //     sq.forEach(operation => {
    //         newSq.push(copy(operation));
    //     });

    //     let transposedElements: CustomOperation[];
    //     for (let i = sq.length - 1; i >= 0; i--) {
    //         transposedElements = transpose(newSq[i], newOp);
    //         newOp = transposedElements[0];
    //         newSq[i] = transposedElements[1];
    //         console.log("transposeOSq");
    //     }

    //     let response = [newOp, newSq];
    //     return response;
    // }

    // Given an operation o and a sequence sq, returns the list of all operations
    // that happened before o and the list of all that happened
    // concurrently with o from sq. Currently looks at stateID, might have to
    // revisit TODO
    // function transposePreCon(op: CustomOperation, sq: CustomOperation[]) {
    //     let msg = "";
    //     sq.forEach((el) => {
    //         msg += el.stateID + " | "
    //     })
    //     console.log(msg)
    //     let happened: CustomOperation[] = [];
    //     let concurrent: CustomOperation[] = [];
    //     let sequences = [];
    //     for (let i = 0; i < sq.length; i++) {
    //         if (sq[i].stateID >= op.stateID) {
    //             concurrent.push(sq[i]);
    //         } else {
    //             let response = transposeOSq(concurrent, sq[i]);
    //             happened.push(response[0]);
    //         }
    //     }
    //     sequences.push(happened);
    //     sequences.push(concurrent);
    //     return sequences;
    // }


    // // Given a sequence sq of insertions and deletions, returns a list of insertions
    // // and a list of deletions.
    // // These lists are transposed so that the effects of the initial sequence sq and
    // // the effect of sqi+sqd is the same
    // // Currently looks at stateID, might have to revisit TODO
    // function transposeInsDel(sq: CustomOperation[]) {
    //     let insertions: CustomOperation[] = [];
    //     let deletions: CustomOperation[] = [];
    //     let sequences: any = [];
    //     for (let i = 0; i < sq.length; i++) {
    //         if (sq[i].type === "remove_text") {
    //             deletions.push(sq[i]);
    //         } else {
    //             let response = transposeOSq(deletions, sq[i]);
    //             insertions.push(response[0]);
    //         }
    //     }
    //     sequences.push(insertions);
    //     sequences.push(deletions);
    //     return sequences;
    // }

    // function buildETSOS(sq: CustomOperation[]) {
    //     if (sq.length < 1) {
    //         return sq;
    //     }
    //     let newSq: CustomOperation[] = [];
    //     let op: CustomOperation;
    //     let flag = true;

    //     newSq.push(sq[0]);
    //     for (let i = 1; i < sq.length; i++) {
    //         op = copy(sq[i]);
    //         flag = false;
    //         for (let j = newSq.length - 1; j >= 0; j--) {
    //             if (flag) {
    //                 let o1Key = { siteID: newSq[j].siteID as number, stateID: newSq[j].stateID as number };
    //                 let o2Key = { siteID: op.siteID as number, stateID: op.stateID as number };
    //                 effectsRelation.set(o1Key, o2Key);
    //             } else {
    //                 if (get_ER_ET(newSq[j], op) === -1) { // if sq[j] is to the left of op

    //                     // PLEASE TEST THIS
    //                     let temp: CustomOperation[] = [];
    //                     for (let k = 0; k <= j; k++) {
    //                         temp.push(newSq[k]);
    //                     }
    //                     temp.push(op);
    //                     for (let k = j + 1; k <= newSq.length - 1; k++) {
    //                         temp.push(newSq[k]);
    //                     }
    //                     newSq = temp;

    //                     flag = true;
    //                 } else {
    //                     let transposed: CustomOperation[] = transpose(newSq[j], op);
    //                     op = transposed[0];
    //                     newSq[j] = transposed[1];
    //                 }
    //             }
    //         }

    //         if (!flag) {
    //             newSq.splice(0, 0, op);
    //         }
    //     }
    //     return newSq;
    // }


    function copy(operation: Operation) {
        //Hacky solution to copy an object
        return JSON.parse(JSON.stringify(operation));
    }



}