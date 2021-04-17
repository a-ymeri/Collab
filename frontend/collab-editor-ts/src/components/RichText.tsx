import React, { useCallback, useMemo, useState } from 'react'
import isHotkey from 'is-hotkey'
import { Editable, withReact, useSlate, Slate } from 'slate-react'
import {
    Editor,
    Transforms,
    createEditor,
    Descendant,
    Node,
    Element as SlateElement,
    SelectionOperation,
} from 'slate'
import { withHistory } from 'slate-history'

import { Button, Icon, Toolbar } from './Components'
import { SetSelectionOperation } from 'slate'
import { faBold } from '@fortawesome/free-solid-svg-icons'

const HOTKEYS = {
    'mod+b': 'bold',
    'mod+i': 'italic',
    'mod+u': 'underline',
    'mod+`': 'code',
}

const LIST_TYPES = ['numbered-list', 'bulleted-list']

const RichTextExample = () => {
    const text = "<p><strong>12345</strong><em>123456789</em></p><p><strong>123</strong></p>";
    //const [value, setValue] = useState<Descendant[]>(initialValue)
    const [value, setValue] = useState<Descendant[]>(parseHtml(text))
    const renderElement = useCallback(props => <Element {...props} />, [])
    const renderLeaf = useCallback(props => <Leaf {...props} />, [])
    const editor = useMemo(() => withHistory(withReact(createEditor())), [])
    const [currSelection, setCurrSelection] = useState(0);

    console.log(initialValue);
    console.log(parseHtml(text));

    function parseHtml(text: String) {
        let tree:SlateElement[] = [];
        // = {
        //     children: [] as any,
        //     type: "paragraph"
        // }

        let element:SlateElement = {
            type: "paragraph",
            children: []
        }
        // let element = {
        //     text : "",
        //     bold: false,
        //     italic: false
        // }

        let span:Node = {
            text:""
            
        }

        let inTagFlag = false;
        let tagText = "";

        let formats = ["<strong>", "<em>"];
        for (let i = 0; i < text.length; i++) {

            let character = "";
            character += text[i];
            //            element = new ArrayList<Leaf>();


            if (character === "\\") {
                i++;
                character += text[i];
            }

            if (character === "<") {
                i++;
                while (text[i] !== '>') {
                    character += text[i];
                    i++;
                }
                character += text[i];

                if (character === "<p>") {

                    element = {
                        type: "paragraph",
                        children: []
                    }
                } else if (character === "</p>") {
                    tree.push(element);
                } else if (character === "<strong>") { // <strong> or <em>
                    span = {
                        text: "",
                        bold: true
                    }
                    tagText = "";
                    inTagFlag = true;
                } else if(character==="<em>"){
                    tagText = "";
                    span = {
                        text: "",
                        italic: true
                    }
                    inTagFlag = true;
                }else { // </strong> or </em>
                    span.text = tagText;
                    element.children.push(span);
                    tagText = "";
                }


            }

            
            if (inTagFlag && text[i] !== '>') {
                tagText += text[i];
            }
        }
        return tree;
    }

        return (
            <Slate editor={editor} value={value} onChange={(val) => {
                setValue(val);
                console.log(JSON.stringify(val));
                const ops = editor.operations;/*.filter(o => {
                console.log(o);
                if (o) { //not undefined

                    return (
                        o.type === 'set_selection'
                        // o.type === "insert_text"
                    );

                }

                return false;
            })*/
                ops.forEach(el=>{
                    if(el.type=='set_selection'){
                        console.log(el);
                    }
                    
                })


            //    let offset = 0; // initial offset, account for <p><whatever_tag>
            //     if (ops.length === 1 && ops[0].type === 'set_selection') {
            //         let row = ops[0].newProperties?.anchor?.path[0] || 0;
            //         let column = ops[0].newProperties?.anchor?.path[1] || 0;

            //         //calculate size of previous rows
            //         for (let i = 0; i < row; i++) {
            //             offset += 2; // Add 2 to offset to account for <p> </p>

            //             for (let j = 0; j < (val[i].children as Array<any>).length; j++) {
            //                 offset += ((val[i].children as Array<any>)[j].text.length);
            //                 offset += 2; //e.g. add 2 to offset to account for <span><span>
            //             }
            //         }

            //         offset++; //Account for start of row symbol TODO: think about end of row </p>

            //         //go through all the columns of current row
            //         for (let i = 0; i < column; i++) {
            //             offset += ((val[row].children as Array<any>)[i].text.length) + 2;
            //         }

            //         offset += (ops[0].newProperties?.anchor?.offset || 0) + 1;

            //     }

            //     console.log(offset);
            //     console.log(val)
                // console.log(val);
            }
            }>
                {/* <Toolbar> */}
                <MarkButton format="bold" icon="format_bold" />
                <MarkButton format="italic" icon="format_italic" />
                <MarkButton format="underline" icon="format_underlined" />
                <MarkButton format="code" icon="code" />
                <BlockButton format="heading-one" icon="looks_one" />
                <BlockButton format="heading-two" icon="looks_two" />
                <BlockButton format="block-quote" icon="format_quote" />
                <BlockButton format="numbered-list" icon="format_list_numbered" />
                <BlockButton format="bulleted-list" icon="format_list_bulleted" />
                {/* </Toolbar> */}
                <Editable
                    renderElement={renderElement}
                    renderLeaf={renderLeaf}
                    placeholder="Enter some rich text…"
                    spellCheck
                    autoFocus
                // onKeyDown={event => {
                //   for (const hotkey in HOTKEYS) {
                //     if (isHotkey(hotkey, event as any)) {
                //       event.preventDefault()
                //       //const mark = HOTKEYS[hotkey]
                //       toggleMark(editor, mark)
                //     }
                //   }
                // }}
                />
            </Slate>
        )
    }



    const toggleBlock = (editor: any, format: any) => {
        const isActive = isBlockActive(editor, format)
        const isList = LIST_TYPES.includes(format)

        Transforms.unwrapNodes(editor, {
            match: n =>
                LIST_TYPES.includes(
                    (!Editor.isEditor(n) && SlateElement.isElement(n) && n.type) as string
                ),
            split: true,
        })
        const newProperties: Partial<SlateElement> = {
            type: isActive ? 'paragraph' : isList ? 'list-item' : format,
        }
        Transforms.setNodes(editor, newProperties)

        if (!isActive && isList) {
            const block = { type: format, children: [] }
            Transforms.wrapNodes(editor, block)
        }
    }

    const toggleMark = (editor: any, format: any) => {
        const isActive = isMarkActive(editor, format)

        if (isActive) {
            Editor.removeMark(editor, format)
        } else {
            Editor.addMark(editor, format, true)
        }
    }

    const isBlockActive = (editor: any, format: any) => {
        const [match] = Editor.nodes(editor, {
            match: n =>
                !Editor.isEditor(n) && SlateElement.isElement(n) && n.type === format,
        })

        return !!match
    }

    const isMarkActive = (editor: any, format: any) => {
        const marks = Editor.marks(editor)
        return marks ? marks[format] === true : false
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

    const BlockButton = ({ format, icon }: any) => {
        const editor = useSlate()
        return (
            <Button
                active={isBlockActive(editor, format)}
                onMouseDown={(event: any) => {
                    event.preventDefault()
                    toggleBlock(editor, format)
                }}
            >
                <Icon>{icon}</Icon>
            </Button>
        )
    }

    const MarkButton = ({ format, icon }: any) => {
        const editor = useSlate()
        return (
            <Button
                active={isMarkActive(editor, format)}
                onMouseDown={(event: any) => {
                    event.preventDefault()
                    toggleMark(editor, format)
                }}
            >
                <Icon>{icon}</Icon>
            </Button>
        )
    }

    const initialValue: SlateElement[] = [
        {
            type: 'paragraph',
            children: [
                { text: 'This is editable ' },
                { text: 'rich', bold: true, italic:false },
                { text: ' text, ', italic:true},
            ],
        },
        {
            type: 'paragraph',
            children: [
                {
                    text: "Child 2",
                }
            ],
        },
    ]

    export default RichTextExample