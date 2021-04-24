import React, { useEffect, useMemo, useRef, useState, useCallback } from 'react'
import { createEditor, InsertNodeOperation, InsertTextOperation, Operation, Path } from 'slate'
import { withHistory } from 'slate-history'
import { Editable, withReact, useSlate, Slate, ReactEditor } from 'slate-react'
import { Button, Icon, Toolbar } from './Components'

// @ts-ignore
import useStateRef from 'react-usestateref'

import {
    Editor,
    Transforms,
    Descendant,
    Node,
    Element as SlateElement,
    SelectionOperation,
} from 'slate'

import * as Stomp from "stompjs";
import SockJS from "sockjs-client";
import { RemoveTextOperation } from 'slate';
import { Cookies } from 'react-cookie';
import { Console } from 'node:console';
import { SplitNodeOperation } from 'slate'
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
type CustomStyleOperation = CustomOperation & { endPath: Path, endOffset: number }

//TODO: Put inside of component
let stateID: number = 0.0;
//TODO: get ID from the back, also UUID
const ID = Date.now();
console.log(ID);

let effectsRelation = new Map<OperationKey, OperationKey>();

//let historyBuffer: CustomOperation[] = []; // List of changes that have been recorded, potentially useful for ETSOS
let toSendBuffer: CustomOperation[] = []; // List of changes TO BE SENT, constantly altered by changes before ACK
let concurrentChanges: CustomOperation[] = []; //List of changes to be sent, unaltered by changes before ACK, used for transformation

let received: CustomOperation[] = [];
export const SyncingEditor: React.FC<Props> = () => {
    const cookies = new Cookies();
    const socket = useRef(new SockJS('http://localhost:8080/collab-editor?access_token=' + cookies.get("auth")))
    const stompClient = useRef(Stomp.over(socket.current));
    //stompClient.current.debug = () => { };


    const [value, setValue, thing] = useStateRef<SlateElement[]>([
        {
            type: 'paragraph',
            children: [{ text: '' }],
        },
    ])

    // const thing = useRef(value);
    const renderElement = useCallback(props => <Element {...props} />, [])
    const renderLeaf = useCallback(props => <Leaf {...props} />, [])
    const editor = useMemo(() => withHistory(withReact(createEditor())), [])


    //const [historyBuffer, setHistoryBuffer] = useState([] as CustomOperation[]);

    const remote = useRef(false);
    const sending = useRef(false);
    // Add the initial value when setting up our state.


    const [text, setText] = useState<String[]>([]);


    useEffect(() => {

        if (!stompClient.current.connected) {

            let jwt: String = cookies.get('auth');
            jwt = jwt.substr(7);
            stompClient.current.connect({ auth: jwt }, function () {
                let id = window.location.href.split('/').pop();

                stompClient.current.subscribe('/app/file/' + id, function (data: Stomp.Message) {

                    let response = JSON.parse(data.body).body;
                    setValue(parseTree(response.text, response.tree));

                    stateID = Number(response.state);
                    setText(toRichText(response.text));
                });


                stompClient.current.subscribe('/topic/' + id, function (data: any) {
                    let op = JSON.parse(data.body);
                    if (op.endOffset !== undefined) {
                        onStyleReceived(op);
                    } else {
                        onReceived(op);
                        received.push(op);
                    }
                });

                stompClient.current.subscribe('/app')
            })
        }

        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [value])

    function operationReceived(data: any) {
        let op = JSON.parse(data.body);
        if (op.endOffset !== undefined) {
            onStyleReceived(op);
        } else {
            onReceived(op);
            received.push(op);
        }
    }

    // function parseHtml(text: String) {
    //     let tree: SlateElement[] = [];
    //     // = {
    //     //     children: [] as any,
    //     //     type: "paragraph"
    //     // }

    //     let element: SlateElement = {
    //         type: "paragraph",
    //         children: []
    //     }
    //     // let element = {
    //     //     text : "",
    //     //     bold: false,
    //     //     italic: false
    //     // }

    //     let span: Node = {
    //         text: ""

    //     }

    //     let inTagFlag = false;
    //     let tagText = "";

    //     let formats = ["<strong>", "<em>"];
    //     for (let i = 0; i < text.length; i++) {

    //         let character = "";
    //         character += text[i];
    //         //            element = new ArrayList<Leaf>();


    //         if (character === "\\") {
    //             i++;
    //             character += text[i];
    //         }

    //         if (character === "<") {
    //             i++;
    //             while (text[i] !== '>') {
    //                 character += text[i];
    //                 i++;
    //             }
    //             character += text[i];

    //             if (character === "<p>") {

    //                 element = {
    //                     type: "paragraph",
    //                     children: []
    //                 }
    //             } else if (character === "</p>") {
    //                 tree.push(element);
    //             } else if (character === "<strong>") { // <strong> or <em>
    //                 span = {
    //                     text: "",
    //                     bold: true
    //                 }
    //                 tagText = "";
    //                 inTagFlag = true;
    //             } else if (character === "<em>") {
    //                 tagText = "";
    //                 span = {
    //                     text: "",
    //                     italic: true
    //                 }
    //                 inTagFlag = true;
    //             }
    //             else if (character === "<span>") {
    //                 tagText = "";
    //                 span = {
    //                     text: "",
    //                 }
    //                 inTagFlag = true;
    //             } else { // </strong> or </em>
    //                 span.text = tagText;
    //                 element.children.push(span);
    //                 tagText = "";
    //             }


    //         }


    //         if (inTagFlag && text[i] !== '>') {
    //             tagText += text[i];
    //         }
    //     }
    //     return tree;
    // }





    function parseTree(text: String, elements: any) {
        let slateTree: SlateElement[] = [];

        let element: SlateElement = {
            type: "paragraph",
            children: []
        }
        // let element = {
        //     text : "",
        //     bold: false,
        //     italic: false
        // }

        // let span: Node = {
        //     text: ""
        // }
        elements = Object.values(elements);
        let cursor = 0;
        elements.forEach((el: any) => {
            console.log(el);
            let arr = el[0].leaves.map((leaf: any, index: number) => {

                let child: Node = {
                    text: text.slice(cursor, cursor + leaf.size),
                }
                cursor += leaf.size;
                leaf.types.forEach((type: any) => {
                    if (type === "strong")
                        child.bold = true;
                    else if (type === "em") {
                        child.italic = true;
                    }
                });
                return child;
            });
            element.children = arr;
            slateTree.push(element);
        })

        return slateTree;
    }


    function onStyleReceived(op: CustomStyleOperation) {
        if (op.siteID !== ID) {
            let tempOp: CustomStyleOperation;

            //If it's below 0, useless operation
            if (op.offset >= 0) {
                for (let i = 0; i < toSendBuffer.length; i++) {
                    tempOp = copy(op);
                    op = inclusionTransformStyle(op, toSendBuffer[i]);
                }
            }

            toSendBuffer.forEach((operation, index) => {
                toSendBuffer[index].stateID++;
            })



            if (op.offset >= 0) {
                remote.current = true;



                let startCursor = getRelativeOffsetStart(op.offset);
                let path = startCursor[0] as Path;
                let offset = startCursor[1] as number;

                let endCursor = getRelativeOffset(op.endOffset);
                let path2 = endCursor[0] as Path;
                let offset2 = endCursor[1] as number;

                let tempSelection = editor.selection;

                console.log("off1: " + op.offset + "   off2:" + op.endOffset)
                console.log("newoff1: " + offset + "   newoff2:" + offset2)
                editor.selection = {
                    focus: { path: path, offset: offset },
                    anchor: { path: path2, offset: offset2 }
                }
                console.log(path)
                console.log("offset:" + offset)
                console.log(path2)
                console.log("offset2:" + offset2)

                const isActive = isMarkActive(editor, op.text)
                if (isActive) {
                    Editor.removeMark(editor, op.text)
                } else {
                    Editor.addMark(editor, op.text, true)
                }
                editor.selection = tempSelection

                //historyBuffer.push(op)
                //setHistoryBuffer(hb => [...hb, op]);
                remote.current = false;
                stateID++;
                console.log("Remote execution of: character:" + op.text + " | offset:" + op.offset + " | stateID: " + op.stateID);
            }
        } else { // ack received
            toSendBuffer.forEach((op, index) => {
                toSendBuffer[index].stateID++;
            })
            toSendBuffer.splice(0, 1);
            concurrentChanges.splice(0, 1);
            sending.current = false;
            if (toSendBuffer.length > 0) {
                sendCharacter(toSendBuffer[0])
            }
            stateID++;
        }
    }

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

                
                let path, offset;
                if(op.type === "insert_text")
                  [path,offset]= getRelativeOffsetInsert(op.offset)
                else
                    [path,offset]= getRelativeOffsetDelete(op.offset)

                //@ts-ignore
                op.path = path
                //@ts-ignore

                op.offset = offset
                editor.apply(op);
                //historyBuffer.push(op)
                //setHistoryBuffer(hb => [...hb, op]);
                remote.current = false;
                stateID++;
                console.log("Remote execution of: character:" + op.text + " | offset:" + op.offset + " | stateID: " + op.stateID);
            }
        } else {
            //Ack received
            toSendBuffer.forEach((op, index) => {
                toSendBuffer[index].stateID++;
            })
            toSendBuffer.splice(0, 1);
            concurrentChanges.splice(0, 1);
            sending.current = false;
            if (toSendBuffer.length > 0) {
                sendCharacter(toSendBuffer[0] as CustomOperation)
            }
            stateID++;
            //historyBuffer.push(op);
        }
    }

    function sendCharacter(operation: CustomOperation | CustomStyleOperation) {
        //stompClient.send("/app/sendcharacter", {}, JSON.stringify({type: operation.type, character: operation.text, index: operation.offset, id: ID, stateID: stateID}));
        if (!sending.current) {
            sending.current = true;

            stompClient.current.send("/app/sendcharacter", {}, JSON.stringify(operation))
            //was setTimeout TODO: remove

        }
    }



    function toRichText(text: string) {
        let a = Date.now();
        if (!text) {
            text = "";
        }
        let newText: string = "";
        let start = 0;
        let inside = false;
        let end = 0;
        let array = [];

        for (let i = 0; i < text.length; i++) {
            if (!inside && text[i] === '&') {
                array.push(text.substr(start, i - start));
                start = i + 1;
                inside = true;
            } else if (inside && text[i] === '&') {
                array.push(text.substr(start, i - start) as any);
                // array[1].bolded = "true";
                start = i + 1;
                inside = false;
            } else {
                newText += text[i];
            }

        }
        array.push(text.substr(start, text.length - start));
        array.forEach((el) => console.log(el));

        let b = Date.now().toPrecision();
        console.log(a);
        console.log(b);
        return array;

    }

    function getAbsoluteOffset(offset: number, path: Path) {
        // initial offset, account for <p><whatever_tag>
        let row = path[0];
        let column = path[1];

        //calculate size of previous rows
        for (let i = 0; i < row; i++) {

            for (let j = 0; j < (value[i].children as Array<any>).length; j++) {
                offset += ((value[i].children as Array<any>)[j].text.length);
            }
        }

        //go through all the columns of current row
        for (let i = 0; i < column; i++) {
            offset += ((value[row].children as Array<any>)[i].text.length);
            console.log((value[row].children as Array<any>)[i].text.length)
        }

        return offset;

    }

    function getRelativeOffset(offset: number) {
        // initial offset, account for <p><whatever_tag>
        let path: Path = [0, 0];
        let found = false;
        //calculate size of previous rows
        for (let i = 0; i < thing.current.length; i++) {
            for (let j = 0; j < (thing.current[i].children as Array<Node>).length; j++) {
                let child = ((thing.current[i].children as Array<Node>)[j] as Node)
                if (offset <= (child.text as string).length) {
                    console.log(j)
                    found = true;
                    console.log('if: ' + j)
                    path[1] = j

                    // console.log(j)
                    // console.log(path[1])
                    break;
                } else {
                    console.log('else: ' + j)
                    offset -= (child.text as string).length
                }
                path[1] = j
            }
            path[0] = i;
            if (found) break;
        }
        console.log(offset)
        return [path as Path, offset as number];
    }

    function getRelativeOffsetInsert(offset: number) {
        // initial offset, account for <p><whatever_tag>
        let path: Path = [0, 0];
        let found = false;
        //calculate size of previous rows
        for (let i = 0; i < thing.current.length; i++) {
            for (let j = 0; j < (thing.current[i].children as Array<Node>).length; j++) {
                let child = ((thing.current[i].children as Array<Node>)[j] as Node)
                if (offset <= (child.text as string).length) {
                    console.log(j)
                    found = true;
                    console.log('if: ' + j)
                    path[1] = j

                    // console.log(j)
                    // console.log(path[1])
                    break;
                } else {
                    console.log('else: ' + j)
                    offset -= (child.text as string).length
                }
                path[1] = j
            }
            path[0] = i;
            if (found) break;
        }
        console.log(offset)
        return [path as Path, offset as number];
    }

    function getRelativeOffsetDelete(offset: number) {
        // initial offset, account for <p><whatever_tag>
        let path: Path = [0, 0];
        let found = false;
        //calculate size of previous rows
        for (let i = 0; i < thing.current.length; i++) {
            for (let j = 0; j < (thing.current[i].children as Array<Node>).length; j++) {
                let child = ((thing.current[i].children as Array<Node>)[j] as Node)
                if (offset < (child.text as string).length) {
                    console.log(j)
                    found = true;
                    console.log('if: ' + j)
                    path[1] = j

                    // console.log(j)
                    // console.log(path[1])
                    break;
                } else {
                    console.log('else: ' + j)
                    offset -= (child.text as string).length
                }
                path[1] = j
            }
            path[0] = i;
            if (found) break;
        }
        console.log(offset)
        return [path as Path, offset as number];
    }


    function getRelativeOffsetStart(offset: number) {
        // initial offset, account for <p><whatever_tag>
        let path: Path = [0, 0];
        let found = false;
        //calculate size of previous rows
        for (let i = 0; i < thing.current.length; i++) {
            for (let j = 0; j < (thing.current[i].children as Array<Node>).length; j++) {
                let child = ((thing.current[i].children as Array<Node>)[j] as Node)
                if (offset < (child.text as string).length) {
                    console.log(j)
                    found = true;
                    console.log('if: ' + j)
                    path[1] = j

                    // console.log(j)
                    // console.log(path[1])
                    break;
                } else {
                    console.log('else: ' + j)
                    offset -= (child.text as string).length
                }
                path[1] = j
            }
            path[0] = i;
            if (found) break;
        }
        console.log(offset)
        return [path as Path, offset as number];
    }

    function destructureOperation(rawOp: any) {
        let text = rawOp.text;
        let state = stateID;
        let op;
        for (let i = 0; i < text.length; i++) {
            op = copy(rawOp);
            op.text = text.charAt(i);
            op.siteID = ID;
            op.stateID = state;
            if (op.type === 'insert_text') {
                op.offset += i;
            }
            op.offset = getAbsoluteOffset(op.offset, op.path)
            toSendBuffer.push(op as CustomOperation);
            concurrentChanges.push(op as CustomOperation);
            // console.log("Local change:" + value[0].children[0].text)
            // console.log("offset: " + op.offset + "| character: " + op.text + "| stateID: " + op.stateID)
            sendCharacter(op);
        }
    }

    function insertNode(op: any, newProperties: any) {
        let text = op.node.text;
        op.offset = newProperties.anchor.offset;
        op.type = 'insert_text';
        op.text = op.node.text;
        destructureOperation(op)
    }

    function changeEditorValue(newValue: any) {
        {
            setValue(newValue as any);

            let tempValue: SlateElement[] = value;
            const ops = editor.operations.filter(o => {
                if (o) { //not undefined
                    console.log(o);
                    return (
                        o.type === 'insert_text' || o.type === 'remove_text' || o.type === 'insert_node' || o.type === 'set_selection' || o.type === 'merge_node' || o.type === 'split_node'
                        // o.type === "insert_text"
                    );

                }

                return false;
            })


            let currentSelection: any;
            ops.forEach((op: any, index, ops) => { //(InsertTextOperation | RemoveTextOperation | InsertNodeOperation)) => {
                if (!op.siteID && editor) { //foreign operation

                    switch (op.type) {
                        //case 'split_node': tempValue = split_node(tempValue as any, op); console.log(tempValue); break;
                        case 'set_selection': currentSelection = op; break;
                        case 'insert_text': destructureOperation(op); break;
                        case 'remove_text': destructureOperation(op); break;
                        case 'insert_node': insertNode(op, editor.selection?.newProperties); break;
                    }
                }
            });
        }
    }

    function split_node(tree: any, operation: SplitNodeOperation) {
        let path = operation.path;
        let element: SlateElement = tree[path[0]]
        let node: Node = element.children[path[1]];
        let text: string = node.text as string;

        let leftNode: Node = { text: text.substr(operation.position) };
        //element.children[path[1]].text = text.substr(0,operation.position)
        if (operation.bold === true) {
            leftNode.bold = true;
        }
        if (operation.underline === true) {
            leftNode.underline = true;
        }
        if (operation.italic === true) {
            leftNode.italic = true;
        }

        let rightNode: Node = { text: text.substr(0, operation.position) };
        //element.children[path[1]].text = text.substr(0,operation.position)
        if (operation.bold === true) {
            rightNode.bold = true;
        }
        if (operation.underline === true) {
            rightNode.underline = true;
        }
        if (operation.italic === true) {
            rightNode.italic = true;
        }
        tree[path[0]].children.splice(path[1], 1, leftNode);
        tree[path[0]].children.splice(path[1] + 1, 0, rightNode);
        return tree;
    }

    return (


        <div className="container-fluid h-100 vh" style={{ position: "absolute" }} >
            <div className="row justify-content-center h-100">
                <div className="col-md-6 padding-0" style={{ backgroundColor: "white" }}>

                    <Slate

                        editor={editor}
                        value={value}
                        onChange={newValue => changeEditorValue(newValue)}
                    >
                        <MarkButton format="bold" icon="BOLD" />
                        <MarkButton format="italic" icon="ITALIC" />
                        <MarkButton format="underline" icon="UNDERLINE" />
                        {/* <MarkButton format="code" icon="code" /> */}
                        <Editable
                            renderElement={renderElement}
                            renderLeaf={renderLeaf}
                            style={{ height: "100%", padding: "10%" }}
                            onKeyDown={event => {

                            }} />
                    </Slate>
                </div>
                <span id="result">

                    {

                        text.map((el) =>
                            <span>
                                {el} <br />
                            </span>
                        )

                    }
                    {
                        //  <div dangerouslySetInnerHTML={createMarkup()} />

                    }

                </span>
            </div>

        </div>
    )


    function createMarkup() {
        return { __html: "<p id='toEdit' onselect='console.log(\"document.getSelection()\")'><strong>12<em>3</em>45</strong><em>123456789</em></p><p><strong>123</strong></p>" };
    }

    function inclusionTransform(o1: CustomOperation, o2: CustomOperation) {
        /*-1 = O1 is to the left of O2, so don't transform
         * 0 = Same position, used for double deletions
         * 1 = O1 is to the right of O2, transform*/

        let relationship: number = get_ER_IT(o1, o2);

        // Clone o1 into newOp1 so we can modify newOp1 without affecting o1
        let newOp1: CustomOperation = copy(o1);


        if (relationship === 0) { // Same position, double deletion
            newOp1.offset = -1;// position = -1 -> Don't delete, identity operation
        } else {
            if (relationship === 1) { // o2 is to the left of o1
                if (o2.type === "insert_text") {// ins = insertion operator
                    newOp1.offset++;
                } else { // o2.type = deletion
                    newOp1.offset--;
                }
            }
        }
        return newOp1;
    }

    function inclusionTransformStyle(o1: CustomStyleOperation, o2: CustomOperation) {

        let relationship = (o1.offset < o2.offset) ? -1 : 1;

        let relationship2 = (o1.endOffset <= o2.offset) ? -1 : 1;

        let newOp1: CustomStyleOperation = copy(o1);

        if (relationship === 1) {
            if (o2.type === "insert_text") {// ins = insertion operator
                newOp1.offset++;
            } else { // o2.type = deletion
                newOp1.offset--;
            }
        }

        if (relationship2 === 1) {
            if (o2.type === "insert_text") {// ins = insertion operator
                newOp1.endOffset++;
            } else { // o2.type = deletion
                newOp1.endOffset--;
            }
        }
        return newOp1

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

    function get_ER_IT_Style(o1: CustomStyleOperation, o2: CustomOperation) {

        // Check if there is a mapping of o1->o2 or o2->o1
        // let o1Key = { siteID: o1.siteID as number, stateID: o1.stateID as number };
        // let o2Key = { siteID: o2.siteID as number, stateID: o2.stateID as number };
        // if (effectsRelation.get(o1Key) === o2Key) {
        //     return -1;
        // }
        // if (effectsRelation.get(o2Key) === o1Key) {
        //     return 1;
        // }

        let relationship = 1;

        // If no existing relationship, create a new one
        if (o1.offset < o2.offset) {
            relationship = -1; // I.e. don't transform
        }
        // } else if (o1.offset === o2.offset) {
        //     // If two insertions, arbitrarily choose by site id, don't transform
        //     if (o1.type === "insert_text" && o2.type === "insert_text" && o1.siteID < o2.siteID) {
        //         relationship = -1;
        //     } else if (o1.type === "remove_text" && o2.type === "remove_text") {
        //         relationship = 0; // Delete only once
        //     } else if (o1.type === "insert_text" && o2.type === "remove_text") {
        //         relationship = -1;
        //     }
        // }
        return relationship;
    }



    function copy(operation: any) {
        //Hacky solution to copy an object
        return JSON.parse(JSON.stringify(operation));
    }

    function toggleMark(editor: ReactEditor, format: any) {
        const isActive = isMarkActive(editor, format)


        let op: CustomOperation;
        if (editor.selection) {
            op = {
                offset: getAbsoluteOffset(editor.selection.anchor.offset, editor.selection.anchor.path),
                endOffset: getAbsoluteOffset(editor.selection.focus.offset, editor.selection.focus.path),
                stateID: stateID,
                siteID: ID,
                path: editor.selection.anchor.path,
                endPath: editor.selection.focus.path,
                text: format,
                type: isActive ? "remove_text" : "insert_text"
                // type:"insert_text"
            }
            stompClient.current.send("/app/changestyle", {}, JSON.stringify(op))
        }


        if (isActive) {
            Editor.removeMark(editor, format)
        } else {
            Editor.addMark(editor, format, true)
        }
    }

    function MarkButton({ format, icon }: any) {
        const editor = useSlate()
        return (
            <Button
                active={isMarkActive(editor, format)}
                onMouseDown={(event: any) => {
                    event.preventDefault()
                    toggleMark(editor, format)
                }
                }
            >
                <Icon>{icon}</Icon>
            </Button>

        )
    }
}


const Element = ({ attributes, children, element }: any) => {
    switch (element.type) {
        case 'block-quote':
            return <blockquote {...attributes}>{children}</blockquote>
        case 'bulleted-list':
            return <ul {...attributes}>{children}</ul>
        case 'heading-one':
            return <h1 {...attributes}>{children}</h1>
        case 'heading-two':
            return <h2 {...attributes}>{children}</h2>
        case 'list-item':
            return <li {...attributes}>{children}</li>
        case 'numbered-list':
            return <ol {...attributes}>{children}</ol>
        default:
            return <p {...attributes}>{children}</p>
    }
}

const Leaf = ({ attributes, children, leaf }: any) => {
    if (leaf.bold) {
        children = <strong>{children}</strong>
    }

    if (leaf.code) {
        children = <code>{children}</code>
    }

    if (leaf.italic) {
        children = <em>{children}</em>
    }

    if (leaf.underline) {
        children = <u>{children}</u>
    }

    return <span {...attributes}>{children}</span>
}






const isMarkActive = (editor: any, format: any) => {
    const marks = Editor.marks(editor)
    return marks ? marks[format] === true : false
}