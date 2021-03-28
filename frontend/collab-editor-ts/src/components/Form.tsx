import { useState } from "react";

interface Props {
    propFields: string[],
    onSubmitFunction: Function,
    formName: string
}
export default function Form({ propFields, onSubmitFunction, formName }: Props) {

    const [fields] = useState<string[]>(propFields);


    return (
        <form className="form-signin" onSubmit={(e) => {
            e.preventDefault();
            //Get all the inputs from the form as strings
            let inputs: string[] = (Array.from(document.getElementsByClassName(formName + "Field")) as HTMLInputElement[]).map(el => el.value);
            onSubmitFunction(inputs);
        }} >

            <h2 className="form-signin-heading">Please {formName}</h2>

            {
                fields.map((element, index) => (
                    <p key={element + index}>
                        <label htmlFor={element} className="sr-only"> {element} </label>
                        <input type={(element.toLowerCase()) === "password" || (element) === "email" ? element : "text"}

                            name={element}
                            className={"form-control " + formName + "Field"} placeholder={element} required />
                    </p>
                ))
            }

            <button className="btn btn-lg btn-primary btn-block" type="submit" id={formName}>{formName}</button>

        </form>
    )
}