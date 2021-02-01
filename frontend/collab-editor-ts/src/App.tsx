
// Import React dependencies.
import React from 'react'
// Import the Slate editor factory.
// import { createEditor } from 'slate'


// // Import the Slate components and React plugin.
// import { Slate, Editable, withReact } from 'slate-react'
import { SyncingEditor } from './SyncingEditor'

// const App = () => {
//   const editor = useMemo(() => withReact(createEditor()), [])
//   // Add the initial value when setting up our state.
//   const [value, setValue] = useState([
//     {
//       type: 'paragraph',
//       children: [{ text: 'A line of text in a paragraph.' }],
//     },
//   ])
//   return (
//     <Slate
//       editor={editor}
//       value={value}
//       onChange={newValue => setValue(newValue as any)}
//     >
//       <Editable
//         onKeyDown={event => {
//           if (event.key === '&') {
//             event.preventDefault();
//             editor.insertText('and');
//           }
//         }} />
//     </Slate>
//   )
// }


const App = () => {
  let array = ["ab", "a/c", "ad"];
  console.log(array.length);
  return (
    <div>
      <SyncingEditor />
    </div>
  )
}


export default App;
