import React, {useEffect, useState} from 'react';
import textlogo from './assets/TextLogo.png';
import {ScrollArea} from "@/components/ui/scroll-area.tsx";
import {FloatedTextInput} from "@/components/ui/FloatedTextInput.tsx";
import {
    Collapsible,
    CollapsibleContent,
    CollapsibleTrigger,
} from "@/components/ui/collapsible.tsx"
import {Button} from "@/components/ui/button.tsx";
import {Label} from "@/components/ui/label.tsx";
import {ChevronsUpDown} from "lucide-react";
import FileTree from "@/components/FileTree.tsx";
import {ResizableHandle, ResizablePanel, ResizablePanelGroup} from "@/components/ui/resizable.tsx";

const Translation: React.FC = () => {
    return (
        <div className="p-5 rounded-md bg-accent flex flex-row gap-4 relative">
            <div className="flex flex-col w-full">
                <Collapsible>
                    <div className="flex flex-row gap-x-3">
                        <div className="w-1/4">
                            <FloatedTextInput label="Translation Id"></FloatedTextInput>
                        </div>
                        <div>
                            <CollapsibleTrigger asChild>
                                <Button size="sm">
                                    <Label>Languages: </Label>
                                    <ChevronsUpDown className="h-4 w-4"/>
                                </Button>
                            </CollapsibleTrigger>
                        </div>
                    </div>
                    <CollapsibleContent>
                        <div className="
                        flex flex-col gap-y-4 m-4">
                            <FloatedTextInput label="fr_FR"></FloatedTextInput>
                            <FloatedTextInput label="Syyyyycecece"></FloatedTextInput>
                            <FloatedTextInput label="fr_FR"></FloatedTextInput>
                            <FloatedTextInput label="fr_FR"></FloatedTextInput>
                            <FloatedTextInput label="fr_FR"></FloatedTextInput>
                            <FloatedTextInput label="fr_FR"></FloatedTextInput>
                        </div>
                    </CollapsibleContent>
                </Collapsible>
            </div>
        </div>
    );
};

const App: React.FC = () => {
    const [paths, setPaths] = useState(["dev.json", "dev/dev.json"]);

    useEffect(() => {
        let ws: WebSocket;

        const websocket = () => {
            const queryParams = new URLSearchParams(window.location.search);
            const websocketUrl = queryParams.get('ws');
            const secret = queryParams.get('secret');
            if (!websocketUrl || !secret) return;
            ws = new WebSocket(websocketUrl);
            console.log("WsUrl: " + websocketUrl)
            ws.onopen = () => {
                console.log("Ws opened")
                const packet = {
                    EditorAuthPacket: {
                        secret: secret
                    }
                }
                ws.send(JSON.stringify(packet))
                setInterval(() => {
                    if (ws.readyState === WebSocket.OPEN) {
                        ws.send('Ping');
                    }
                }, 2000);
            }
            ws.onmessage = e => {
                const message: string = e.data;
                const packet = JSON.parse(message);
                console.log("Packet", packet)
                const packetType = Object.keys(packet)[0];
                const packetValues = packet[packetType];
                switch (packetType) {
                    case "EditorInfosPacket":
                        // eslint-disable-next-line no-case-declarations
                        const pathList = packetValues.translationFilesPaths;
                        setPaths([...pathList]);
                        console.log(pathList)
                        break;
                }

            }
            ws.onclose = async (e) => {
                console.log("closeEvent", e)
                console.log("Reconnection...")
                setTimeout(() => {
                    if (ws.readyState !== WebSocket.OPEN) websocket();
                }, 3000)
            }
        }

        websocket();

        return () => {
            ws.close();
        }
    }, [])

    return (
        <div className="flex flex-row h-screen bg-background">
            <ResizablePanelGroup direction="horizontal">
                <ResizablePanel defaultSize={20} minSize={15} maxSize={50}>
                    <div className="bg-background border-r-2 border-accent h-full">
                        <div className="mx-5 max-w-[30rem]">
                            <img src={textlogo}></img>
                        </div>
                        <ScrollArea>
                            <FileTree key={paths.join(',')} paths={paths}
                                      onFileClick={(e) => {
                                          console.log(e);
                                      }}
                                      onFolderClick={(e) => {
                                          console.log(e);
                                      }}
                            />
                        </ScrollArea>
                    </div>
                </ResizablePanel>
                <ResizableHandle withHandle/>
                <ResizablePanel>
                    <ScrollArea className="w-full">
                        <div className="h-full w-full flex flex-col gap-3 p-5">
                            <Translation/>
                        </div>
                    </ScrollArea>
                </ResizablePanel>
            </ResizablePanelGroup>
        </div>
    );
};

export default App;