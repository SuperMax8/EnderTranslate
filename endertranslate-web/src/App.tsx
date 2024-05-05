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
import {ChevronsUpDown, File, Settings, X} from "lucide-react";
import FileTree from "@/components/FileTree.tsx";
import {ResizableHandle, ResizablePanel, ResizablePanelGroup} from "@/components/ui/resizable.tsx";
import {TranslationEntry, TranslationFile} from "@/TranslationFile.tsx";
import {
    AlertDialog,
    AlertDialogAction, AlertDialogCancel,
    AlertDialogContent, AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTrigger
} from "@/components/ui/alert-dialog.tsx";
import {useToast} from "@/components/ui/use-toast.tsx";
import {Toaster} from "@/components/ui/toaster.tsx";

let ws: WebSocket;
let languages: Array<string>;
let currentPagePath: string;

let saveTranslation;
let secret;

const TranslationComp: React.FC<{ entry: TranslationEntry, deleteEntry: (entry: TranslationEntry) => void }> = ({
                                                                                                                    entry,
                                                                                                                    deleteEntry
                                                                                                                }) => {
    console.log(languages)
    return (
        <div className="p-5 rounded-md bg-accent flex flex-row gap-4 relative">
            <div className="flex flex-col w-full">
                <Collapsible>
                    <div className="flex flex-row gap-x-3 items-center">
                        <div className="w-1/4">
                            <FloatedTextInput label="Translation Id" defaultValue={entry.id}
                                              onChange={e => {
                                                  entry.id = e.target.value;
                                                  saveTranslation()
                                              }}/>
                        </div>
                        <div>
                            <CollapsibleTrigger asChild>
                                <Button size="sm">
                                    <Label>Languages: </Label>
                                    <ChevronsUpDown className="h-4 w-4"/>
                                </Button>
                            </CollapsibleTrigger>
                        </div>
                        <AlertDialog>
                            <AlertDialogTrigger asChild>
                                <Button size="sm" variant="destructive">
                                    <X className="h-4 w-4"/>
                                </Button>
                            </AlertDialogTrigger>
                            <AlertDialogContent>
                                <AlertDialogHeader>Are you sure you want to delete this translation?</AlertDialogHeader>
                                <AlertDialogFooter>
                                    <AlertDialogCancel>Cancel</AlertDialogCancel>
                                    <AlertDialogAction className="text-red-600"
                                                       onClick={() => deleteEntry(entry)}>Delete</AlertDialogAction>
                                </AlertDialogFooter>
                            </AlertDialogContent>
                        </AlertDialog>
                    </div>
                    <CollapsibleContent>
                        <div className="flex flex-col gap-y-4 m-4">
                            {
                                languages.map((languageId) => (
                                    <div key={languageId + entry.id}>
                                        <FloatedTextInput
                                            label={languageId}
                                            key={languageId + entry.id}
                                            defaultValue={entry.values.get(languageId) ? entry.values.get(languageId) : ''}
                                            onChange={e => {
                                                entry.values.set(languageId, e.target.value)
                                                saveTranslation()
                                            }}
                                        />
                                    </div>
                                ))
                            }
                        </div>
                    </CollapsibleContent>
                </Collapsible>
            </div>
        </div>
    );
};

const TranslationFileComp: React.FC<{
    id: string,
    translationFile: TranslationFile | undefined,
    addNewEntry: () => void
    deleteEntry: (entry: TranslationEntry) => void
}> = ({
          id,
          translationFile,
          addNewEntry,
          deleteEntry
      }) => {
    if (translationFile) {

        return (
            <div className="flex flex-col flex-grow h-full">

                <div className="text-4xl
                underline decoration-gray-200 decoration-4
                flex flex-row items-center gap-x-4
                w-fit
                m-5
                p-1
                ring-4 hover:ring-blue-300 transition-all
                rounded-lg
                ">
                    <File className="h-14 w-14"/>
                    <h1>{id}</h1>
                </div>

                <ScrollArea>
                    <div className="flex flex-col m-5 gap-y-5 h-max">
                        {translationFile.entries.map(entry => (
                            // eslint-disable-next-line react-hooks/rules-of-hooks
                            <TranslationComp key={React.useId()} entry={entry} deleteEntry={deleteEntry}/>
                        ))}
                        <div className="flex w-full justify-center">
                            <Button onClick={() => {
                                addNewEntry()
                            }}>
                                Create a new translation
                            </Button>
                        </div>
                    </div>
                </ScrollArea>
            </div>
        )
    } else {
        return (
            <div className="w-full h-full flex justify-center items-center">
                <h1 className="text-5xl font-bold text-center
                hover:text-6xl transition-all p-10">Click on a translation file in the tree to open it</h1>
            </div>
        )
    }
}

const App: React.FC = () => {
    const [paths, setPaths] = useState(["loading.json", "the/fileTree.json", "if you have time to read it, it means that the websocket doesn't connect correctly, check your plugin config"]);
    const [translationPage, setTranslationPage] = useState<TranslationFile>();
    let newEntrySpamCount = 0;

    const addNewEntry = () => {
        if (translationPage?.entries.some(entry => entry.id == "")) {
            if (newEntrySpamCount === 4) {
                newEntrySpamCount = 0;
                // Rick Roll
                window.open("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
            }
            newEntrySpamCount++;
            return;
        }
        newEntrySpamCount = 0;
        const newEntry = new TranslationEntry("", new Map());
        translationPage?.entries.push(newEntry);
        setTranslationPage(new TranslationFile(translationPage?.entries));
        saveTranslation();
    };

    const deleteEntry = (entry: TranslationEntry) => {
        translationPage?.entries.splice(translationPage?.entries.indexOf(entry), 1);
        setTranslationPage(new TranslationFile(translationPage?.entries));
        saveTranslation();
    }

    saveTranslation = () => {
        const packet = {
            "EditorSaveFilePacket": {
                path: currentPagePath,
                data: translationPage?.toJSON()
            }
        }
        ws.send(JSON.stringify(packet))
    }

    useEffect(() => {

        const websocket = () => {
            const queryParams = new URLSearchParams(window.location.search);
            const websocketUrl = queryParams.get('ws');
            secret = queryParams.get('secret');
            if (!websocketUrl || !secret) return;
            ws = new WebSocket(websocketUrl);
            console.log("WsUrl: " + websocketUrl)
            ws.onopen = () => {
                console.log("Ws opened")
                const packet = {
                    "EditorAuthPacket": {
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
                        languages = packetValues.languages;
                        console.log(pathList)
                        break;
                    case "EditorPagePacket":
                        setTranslationPage(TranslationFile.fromJSON(packetValues.file));
                        console.log("Set page", packetValues.file)
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

    const {toast} = useToast()

    return (
        <div className="flex flex-row h-screen bg-background">
            <ResizablePanelGroup direction="horizontal">
                <ResizablePanel defaultSize={20} minSize={15} maxSize={50}>
                    <div className="bg-background border-r-2 border-accent h-full">
                        <div className="flex items-center justify-center flex-col pb-14">
                            <div className="mx-5 max-w-[30rem]">
                                <img src={textlogo}></img>
                            </div>
                            <div className="">
                                <Button className="w-fit" onClick={() => {
                                    ws.send(JSON.stringify({
                                        "ReloadPluginPacket": {}
                                    }))
                                    toast({
                                        title: "Plugin reloaded !"
                                    })
                                    console.log("Plugin reloaded")
                                }}>
                                    Reload Plugin <Settings/>
                                </Button>
                            </div>
                        </div>
                        <ScrollArea className="h-full">
                            <FileTree key={paths.join(';')} initialPaths={paths}
                                      onFileClick={(e) => {
                                          currentPagePath = e.join('/');
                                          const packet = {
                                              "EditorPagePacket": {
                                                  "pageRelativePath": currentPagePath,
                                                  "file": null,
                                              }
                                          };
                                          ws.send(JSON.stringify(packet));
                                      }}
                                      onFolderClick={(e) => {
                                          console.log(e);
                                      }}
                                      deleteProvider={(path) => {
                                          const packet = {
                                              "EditorDeleteFilePacket": {
                                                  "path": path,
                                              }
                                          };
                                          ws.send(JSON.stringify(packet));
                                          const packet2 = {
                                              "EditorAuthPacket": {
                                                  secret: secret
                                              }
                                          }
                                          ws.send(JSON.stringify(packet2))
                                          setTranslationPage(undefined)
                                      }}
                                      renameProvider={(path, newPath) => {
                                          const packet = {
                                              "EditorRenamePacket": {
                                                  "path": path,
                                                  newPath: newPath
                                              }
                                          };
                                          ws.send(JSON.stringify(packet));
                                          const packet2 = {
                                              "EditorAuthPacket": {
                                                  secret: secret
                                              }
                                          }
                                          ws.send(JSON.stringify(packet2))
                                          setTranslationPage(undefined)
                                      }}
                                      createFileProvider={path => {
                                          const packet = {
                                              "EditorCreateFilePacket": {
                                                  "path": path,
                                              }
                                          };
                                          ws.send(JSON.stringify(packet));
                                          const packet2 = {
                                              "EditorAuthPacket": {
                                                  secret: secret
                                              }
                                          }
                                          ws.send(JSON.stringify(packet2))
                                          setTranslationPage(undefined)
                                      }}
                                      moveFileProvider={(from: string, to: string) => {
                                          const packet = {
                                              "EditorMoveFilePacket": {
                                                  "fromPath": from,
                                                  "toPath": to,
                                              }
                                          };
                                          ws.send(JSON.stringify(packet));
                                          const packet2 = {
                                              "EditorAuthPacket": {
                                                  secret: secret
                                              }
                                          }
                                          ws.send(JSON.stringify(packet2))
                                          setTranslationPage(undefined)
                                      }}
                            />
                        </ScrollArea>
                    </div>

                </ResizablePanel>
                <ResizableHandle withHandle/>
                <ResizablePanel>
                    <TranslationFileComp key={translationPage?.toJSON()} id={currentPagePath}
                                         translationFile={translationPage} addNewEntry={addNewEntry}
                                         deleteEntry={deleteEntry}/>
                </ResizablePanel>
            </ResizablePanelGroup>
            <Toaster />
        </div>
    );
};

export default App;