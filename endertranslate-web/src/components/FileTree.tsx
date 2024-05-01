import {useState} from "react";
import {ChevronDown, ChevronRight, File, Folder} from "lucide-react";

interface TreeNode {
    name: string;
    isOpen: boolean;
    children: { [name: string]: TreeNode };
}

function buildTree(paths: string[]): TreeNode {
    const root: TreeNode = {name: "root", isOpen: true, children: {}};

    paths.forEach(path => {
        const parts = path.split('/');
        let current = root;
        parts.forEach(part => {
            if (!current.children[part]) {
                current.children[part] = {name: part, isOpen: false, children: {}};
                console.log("create " + part);
            }
            current = current.children[part];
        });
    });

    return root;
}

interface FileTreeProps {
    paths: string[];
    onFolderClick: (path: string[]) => void;
    onFileClick: (path: string[]) => void;
}

const FileTree: React.FC<FileTreeProps> = ({paths, onFolderClick, onFileClick}) => {
    // Création de l'arbre à partir des chemins et gestion de l'état de cet arbre
    const [tree, setTree] = useState(() => buildTree(paths));

    const toggleFolder = (nodePath: string[]) => {
        // Création d'une copie profonde pour pouvoir modifier l'état
        const newTree = JSON.parse(JSON.stringify(tree)); // Une simple approche de copie profonde
        let current = newTree;
        nodePath.forEach(part => {
            current = current.children[part];
        });
        current.isOpen = !current.isOpen;
        setTree(newTree); // Mise à jour de l'état de l'arbre
    };

    const renderTree = (node: TreeNode, path: string[] = []) => (
        <div>
            <ul>
                {Object.keys(node.children).map(key => {
                    const hasChildren = Object.keys(node.children[key].children).length > 0;
                    return (
                        <li key={key} className="pl-4 cursor-pointer">
                            <div onClick={() => {
                                if (hasChildren) {
                                    toggleFolder([...path, key]);
                                    onFolderClick([...path, key]);
                                } else {
                                    onFileClick([...path, key]);  // Déclencher le callback pour les fichiers ici
                                }
                            }}>
                                <div className="flex flex-row items-center gap-x-1">
                                    {hasChildren || !node.name.includes(".json") ?
                                        <div className="flex flex-row items-center gap-x-1">
                                            {(node.children[key].isOpen ? <ChevronDown/> : <ChevronRight/>)}
                                            <Folder/>
                                        </div>
                                        :
                                        <File/>
                                    }
                                    {key}
                                </div>
                            </div>

                            {node.children[key].isOpen && renderTree(node.children[key], [...path, key])}

                        </li>
                    );
                })}
            </ul>
        </div>
    );

    return <div className="p-5 text-2xl">
        <h1 className="text-2xl
        border-b-4
        mb-5 pb-3 w-full">Translation Files</h1>
        {renderTree(tree)}
    </div>;
};

export default FileTree;