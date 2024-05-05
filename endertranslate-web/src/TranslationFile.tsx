export class TranslationEntry {
    _id: string;
    public values: Map<string, string>;

    constructor(id: string, values: Map<string, string>) {
        this._id = id;
        this.values = values;
    }

    public get id(): string {
        return this._id;
    }

    public set id(value: string) {
        this._id = value;
    }
}

export class TranslationFile {
    private _entries: TranslationEntry[];

    constructor(entries: TranslationEntry[] | undefined) {
        if (entries)
            this._entries = entries;
        else this._entries = [];
    }


    get entries(): TranslationEntry[] {
        return this._entries;
    }

    static fromJSON(jsonObject: any): TranslationFile {
        if (!Array.isArray(jsonObject.entries)) {
            console.log(jsonObject)
            throw new Error("Expected 'entries' to be an array.");
        }

        const entries: TranslationEntry[] = jsonObject.entries.map(entry => {
            if (typeof entry.id !== 'string' || typeof entry.values !== 'object') {
                throw new Error("Invalid format for TranslationEntry.");
            }

            const mapValues = new Map<string, string>();
            for (const key in entry.values) {
                const value = entry.values[key];
                if (typeof value !== 'string') {
                    throw new Error("Expected each value to be a string.");
                }
                mapValues.set(key, value);
            }

            return {"id": entry.id, "values": mapValues};
        });

        return new TranslationFile(entries);
    }

    toJSON(): string {
        const object = {
            "entries": this._entries.map(entry => ({
                "id": entry.id,
                "values": Object.fromEntries(entry.values)
            }))
        };
        return JSON.stringify(object);
    }

}