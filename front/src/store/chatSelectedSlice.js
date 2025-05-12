import { createSlice } from '@reduxjs/toolkit';

const initialState = {
    id: null,
    username: "",
    name: "",
    private: null,
    new: null,
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
            state.messages = [];
        },
        addMessages: (state, action) => {
            state.messages = [...action.payload, ...state.messages];
        },
        postMessage: (state, action) => {
            state.messages = [...state.messages, action.payload];
        }
    },
});

export const { setChat, addMessages, postMessage } = chatSelectedSlice.actions;
export default chatSelectedSlice.reducer;