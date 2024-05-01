import { JSX, useState } from "react";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible";
import { cn } from "@/lib/utils";
import {ChevronsUpDown} from "lucide-react";

interface ExpandableContainerProps {
    title: JSX.Element | string;
    children: JSX.Element | JSX.Element[];
    className?: string | null;
}

export default function ExpandableContainer({ title, children, className }: ExpandableContainerProps) {
    const [isOpen, setIsOpen] = useState(false);

    return (
        <Collapsible open={isOpen} onOpenChange={setIsOpen} className={cn(className)}>
            <CollapsibleTrigger className="flex justify-between items-center w-full">
                {title}
                <ChevronsUpDown
                    size="1rem"
                    style={{
                        transform: isOpen ? `rotate(90deg)` : "none",
                    }}
                />
            </CollapsibleTrigger>
            <CollapsibleContent className="py-2 mt-2 border-t-2 transition-all data-[state=closed]:animate-collapsible-up data-[state=open]:animate-collapsible-down">
                {children}
            </CollapsibleContent>
        </Collapsible>
    );
}