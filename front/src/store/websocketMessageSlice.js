import { createSlice } from '@reduxjs/toolkit';


const initialState = {
    message: null
};

export const websocketMessageSlice = createSlice({
    name: "websocketMessage",
    initialState,
    reducers: {
        setMessage: (state, action) => {
            state.message = action.payload;
        }
    },
});

export const { setMessage } = websocketMessageSlice.actions;
export default websocketMessageSlice.reducer;