import React, {useEffect, useState} from "react";
import {ChevronDown, ChevronRight, File, FilePlus, Folder, FolderPlus} from "lucide-react";
import {ContextMenu, ContextMenuContent, ContextMenuItem, ContextMenuTrigger} from "@/components/ui/context-menu.tsx";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import {FloatedTextInput} from "@/components/ui/FloatedTextInput.tsx";
import {useDrag, useDrop} from 'react-dnd';

interface TreeNode {
    name: string;
    isOpen: boolean;
    isDirectory: boolean;
    children: { [name: string]: TreeNode };
}

function buildTree(paths: string[], openFolders: string[]): TreeNode {
    const root: TreeNode = {name: "root", isOpen: true, isDirectory: true, children: {}};

    paths.forEach(path => {
        const parts = path.split('/');
        let current = root;
        let currentPath = "";

        parts.forEach(part => {
            currentPath = currentPath ? `${currentPath}/${part}` : part;

            if (!current.children[part]) {
                current.children[part] = {
                    name: part,
                    isOpen: openFolders.includes(currentPath),
                    isDirectory: !part.endsWith(".json"),
                    children: {}
                };
                console.log("create " + part, !part.endsWith(".json"));
            }
            current = current.children[part];
        });
    });

    return root;
}

interface FileTreeProps {
    initialPaths: string[];
    onFolderClick: (path: string[]) => void;
    onFileClick: (path: string[]) => void;
    deleteProvider;
    renameProvider;
    createFileProvider;
    moveFileProvider
}

const FileTree: React.FC<FileTreeProps> = ({
                                               initialPaths,
                                               onFolderClick,
                                               onFileClick,
                                               deleteProvider,
                                               renameProvider,
                                               createFileProvider,
                                               moveFileProvider
                                           }) => {
    const [paths, setPaths] = useState(initialPaths);
    const [openFolders, setOpenFolders] = useState<string[]>(() => {
        // Charger les données depuis localStorage au montage
        return JSON.parse(localStorage.getItem("openFolders") || "[]");
    });
    const [tree, setTree] = useState(() => buildTree(initialPaths, openFolders));

    useEffect(() => {
        setTree(buildTree(paths, openFolders));
    }, [paths, openFolders]);

    useEffect(() => {
        const savedOpenFolders = JSON.parse(localStorage.getItem("openFolders") || "[]");
        setOpenFolders(savedOpenFolders);
    }, []);

    useEffect(() => {
        localStorage.setItem("openFolders", JSON.stringify(openFolders));
    }, [openFolders]);

    /*    const reOpenFolders = () => {
            setTimeout(() => {
                console.log('reOpenFolders')
                open.forEach(openPath => {
                    console.log("REloading " + openPath)
                    toggleFolder(openPath.split("/"), false);
                })
            }, 100)
        }*/

    const addRootFile = () => {
        const newFileName = "New File.json"; // Exemple de nouveau fichier
        let newName = newFileName;
        let count = 1;
        while (paths.includes(newName)) {
            newName = count + newFileName;
            count++;
        }
        setPaths([...paths, newName]);
        createFileProvider(newName);
    };

    const addRootFolder = () => {
        const newFolderName = "New Folder";
        let newName = newFolderName;
        let count = 1;
        while (paths.includes(newName)) {
            newName = newFolderName + count;
            count++;
        }
        setPaths([...paths, newName]);
        createFileProvider(newName);
        /*        reOpenFolders()*/
    };

    const rename = (path: string, newName: string) => {
        paths.splice(paths.indexOf(path), 1)
        const newPath = path.substring(0, path.lastIndexOf("/") + 1) + newName;
        console.log("RenameTo: " + newPath)
        paths.push(newPath)
        setPaths([...paths]);
        console.log("Path " + path + " NewPath: " + newPath)
        renameProvider(path, newPath);
    }

    const toggleFolder = (nodePath: string[]) => {
        console.log("ToggleFolder: " + nodePath);
        const path = nodePath.join("/");

        setOpenFolders(prev => {
            const updatedFolders = prev.includes(path)
                ? prev.filter(p => p !== path) // Fermer le dossier
                : [...prev, path]; // Ouvrir le dossier

            // Sauvegarde dans localStorage
            localStorage.setItem("openFolders", JSON.stringify(updatedFolders));
            return updatedFolders;
        });
        // Création d'une copie profonde pour pouvoir modifier l'état
        const newTree = JSON.parse(JSON.stringify(tree)); // Une simple approche de copie profonde
        let current = newTree;
        nodePath.forEach(part => {
            current = current.children[part];
        });
        current.isOpen = !current.isOpen;
        setTree(newTree); // Mise à jour de l'état de l'arbre
    };

    const moveFile = (fromPath, inFolderPath) => {
        // Logique pour déplacer un fichier ou dossier
        console.log("fromPath " + fromPath + " inFolderPath " + inFolderPath)
        const fileName = fromPath.includes("/") ? fromPath.substring(fromPath.lastIndexOf("/") + 1, fromPath.length) : fromPath;
        const newFilePath = inFolderPath === "" ? fileName : inFolderPath + "/" + fileName;

        moveFileProvider(fromPath, newFilePath)
        console.log("move from " + fromPath + " to " + newFilePath);
    };

    const renderTree = (node: TreeNode, path: string[] = []) => (
        <div>
            <ul>
                {Object.keys(node.children).map(key => {
                    const hasChildren = Object.keys(node.children[key].children).length > 0;
                    return (
                        <li key={key} className="ml-14 cursor-pointer">
                            <FileElement node={node} parentPath={path} fileName={key}
                                         hasChildren={hasChildren}></FileElement>
                            {node.children[key].isOpen && renderTree(node.children[key], [...path, key])}
                        </li>
                    );
                })}
            </ul>
        </div>
    );

    const Root = (props) => {
        const [, dropRef] = useDrop(() => ({
            accept: 'FILE',
            drop: (item: { path: string[] }) => {
                console.log("Root")
                moveFile(item.path.join("/"), "")
            },
            collect: monitor => ({
                isOver: !!monitor.isOver(),
                canDrop: !!monitor.canDrop(),
            }),
        }));

        return (
            <div ref={dropRef} className="h-32">
                {props.children}
            </div>
        );
    }

    const FileElement: React.FC<{ node, parentPath, fileName, hasChildren }> = ({
                                                                                    node,
                                                                                    parentPath,
                                                                                    fileName,
                                                                                    hasChildren
                                                                                }) => {
        const [openDelete, setOpenDelete] = useState(false);
        const [openRename, setOpenRename] = useState(false);
        let renameValue;


        const canMoveFile = (fromPath: Array<string>, toPath: Array<string>) => {
            const fromPathString = fromPath.join('/');
            const toPathString = toPath.join('/');
            const fileName = fromPathString.includes("/") ? fromPathString.substring(fromPath.lastIndexOf("/"), fromPathString.length) : fromPathString;
            const possibleDuplicate = toPath + "/" + fileName;

            console.log(fromPathString, toPathString)
            return (fromPathString !== toPathString) && !toPathString.endsWith(".json") &&
                !paths.some(el => el === possibleDuplicate);
        };


        const [, drag] = useDrag(() => ({
            type: 'FILE',
            item: {path: [...parentPath, fileName]},
            collect: monitor => ({
                isDragging: !!monitor.isDragging(),
            }),
        }));

        const [{canDrop, isOver}, drop] = useDrop(() => ({
            accept: 'FILE',
            drop: (item: { path: string[] }) => {
                const toDir = [...parentPath, fileName].join("/");
                console.log("ToDir " + toDir)
                moveFile(item.path.join("/"), toDir)
            },
            canDrop: (item: { path: string[] }) => canMoveFile(item.path, [...parentPath, fileName]),
            collect: monitor => ({
                isOver: !!monitor.isOver(),
                canDrop: !!monitor.canDrop(),
            }),
        }));

        const isActive = canDrop && isOver;
        let backgroundColor = '';
        if (isActive) {
            backgroundColor = 'text-blue-600';
        } else if (canDrop) {
            backgroundColor = 'text-green-500';
        }

        return (
            <div className={backgroundColor} ref={node.isDirectory ? drop : null}>
                <div ref={drag}>
                    <ContextMenu>
                        <ContextMenuTrigger>
                            <div
                                onClick={() => {
                                    if (hasChildren) {
                                        toggleFolder([...parentPath, fileName]);
                                        onFolderClick([...parentPath, fileName]);
                                    } else {
                                        onFileClick([...parentPath, fileName]);  // Déclencher le callback pour les fichiers ici
                                    }
                                }}
                                className="flex flex-row items-center gap-x-1 hover:scale-110 transition-all">
                                {node.children[fileName].isDirectory ?
                                    <div className="flex flex-row items-center gap-x-1">
                                        {hasChildren ? (node.children[fileName].isOpen ?
                                            <ChevronDown/> :
                                            <ChevronRight/>) : ''}
                                        <Folder/>
                                    </div>
                                    :
                                    <div className="">
                                        <File/>
                                    </div>
                                }
                                {fileName}
                            </div>

                        </ContextMenuTrigger>
                        <ContextMenuContent>
                            <ContextMenuItem
                                onClick={() => setOpenRename(true)}>Rename</ContextMenuItem>
                            <ContextMenuItem onClick={() => {
                                setOpenDelete(true)
                            }}>
                                <p className="text-red-600">Delete</p>
                            </ContextMenuItem>
                        </ContextMenuContent>
                    </ContextMenu>

                    <AlertDialog open={openDelete} onOpenChange={setOpenDelete}>
                        <AlertDialogContent>
                            <AlertDialogHeader>
                                <AlertDialogTitle>Are you sure you want to delete this file?</AlertDialogTitle>
                                <AlertDialogDescription>
                                    This will delete the file/folder {fileName}
                                </AlertDialogDescription>
                            </AlertDialogHeader>
                            <AlertDialogFooter>
                                <AlertDialogCancel onClick={() => {
                                    setOpenDelete(false);
                                }}>Cancel</AlertDialogCancel>
                                <AlertDialogAction onClick={() => {
                                    setOpenDelete(false);
                                    const pathh = [...parentPath, fileName].join("/");
                                    paths.splice(paths.indexOf(pathh), 1)
                                    setPaths([...paths])
                                    deleteProvider(pathh)
                                }} className="text-red-600">Delete</AlertDialogAction>
                            </AlertDialogFooter>
                        </AlertDialogContent>
                    </AlertDialog>

                    <AlertDialog open={openRename}>
                        <AlertDialogContent>
                            <AlertDialogHeader>
                                <AlertDialogTitle>Rename file</AlertDialogTitle>
                                <FloatedTextInput label="File name" defaultValue={fileName.replace(".json", "")}
                                                  onChange={event => renameValue = event.target.value}/>
                            </AlertDialogHeader>
                            <AlertDialogFooter>
                                <AlertDialogCancel onClick={() => {
                                    setOpenRename(false);
                                }}>Cancel</AlertDialogCancel>
                                <AlertDialogAction onClick={() => {
                                    setOpenRename(false);
                                    rename([...parentPath, fileName].join("/"), fileName.includes(".json") ? renameValue + ".json" : renameValue)
                                }}>Rename</AlertDialogAction>
                            </AlertDialogFooter>
                        </AlertDialogContent>
                    </AlertDialog>
                </div>
            </div>
        )
    }

    return <div className="p-5 text-2xl">
        <div className="
        flex flex-row items-center gap-x-3
        border-b-4
        mb-5 pb-3 w-full">
            <h1 className="text-2xl">Translation Files</h1>
            <FilePlus className="h-8 w-8 hover:scale-110 transition-all" onClick={addRootFile}/>
            <FolderPlus className="h-8 w-8 hover:scale-110 transition-all" onClick={addRootFolder}/>
        </div>
        {renderTree(tree)}
        <Root/>
    </div>;
}

export default FileTree;