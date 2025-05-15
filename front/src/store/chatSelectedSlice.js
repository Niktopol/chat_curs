import { createSlice } from '@reduxjs/toolkit';

const initialState = {
    id: null,
    username: "",
    name: "",
    private: null,
    new: null,
    image: "/default_user.svg",
    users: [],
    messages: []
};

export const chatSelectedSlice = createSlice({
    name: "chatSelected",
    initialState,
    reducers: {
        setChat: (state, action) => {
            state.id = action.payload.id;
            state.username = action.payload?.username;
            state.name = action.payload.name;
            state.private = action.payload.private;
            state.new = action.payload.new;
            state.image = action.payload.image;
        },
        addMessages: (state, action) => {
            state.messages = [...action.payload, ...state.messages];
        },
        postMessage: (state, action) => {
            state.messages = [...state.messages, action.payload];
        },
        setUsers: (state, action) => {
            state.users = [...action.payload]
        }
    },
});

export const { setChat, addMessages, postMessage, setUsers } = chatSelectedSlice.actions;
export default chatSelectedSlice.reducer;