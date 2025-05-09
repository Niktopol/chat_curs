import { createAsyncThunk } from "@reduxjs/toolkit";


export const fetchUserSession = createAsyncThunk(
    'userSession/fetch',
    async (_, thunkAPI) => {
        try {
            const response = await fetch("http://localhost:8080/profile", {credentials: "include"});
            if (!response.ok) throw new Error("Unable to fetch session");
            return await response.json();
        } catch (error) {
            return thunkAPI.rejectWithValue(error.message);
        }
    }
);