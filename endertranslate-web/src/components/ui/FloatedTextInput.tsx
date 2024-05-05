import * as React from "react"

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    label: string;
}

const FloatedTextInput = React.forwardRef<HTMLInputElement, InputProps>(
    ({label, ...props}, ref) => {
        const inputId = React.useId();

        return (
            <div>
                <div className="relative">
                    <input id={inputId} type="text"
                           className="z-10 flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm
                       ring-offset-background
                       placeholder:text-transparent
                       focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2
                       disabled:cursor-not-allowed disabled:opacity-50
                       peer"
                           placeholder="Placeholder"
                           ref={ref}
                           {...props}/>
                    <label htmlFor={inputId} className="select-none bg-background px-2 rounded-md text-sm text-muted-foreground absolute
                -top-3 left-2
                    peer-placeholder-shown:top-2.5
                    peer-focus:-top-3
                    transition-all
                    z-10
                    origin-[0]
                    ">
                        {label}
                    </label>
                </div>
            </div>
        )
    }
)


FloatedTextInput.displayName = "Input"

export {FloatedTextInput}