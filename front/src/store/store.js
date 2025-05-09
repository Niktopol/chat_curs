import { configureStore } from '@reduxjs/toolkit';
import userSessionReducer from './userSessionSlice';

export const store = configureStore({
  reducer: {
    userSession: userSessionReducer,
  },
});